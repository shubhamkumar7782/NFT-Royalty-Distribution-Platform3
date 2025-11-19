
"""
contract_service.py - Smart Contract Business Logic Service
Python equivalent to ContractService.java - uses Web3.py (Python's equivalent to web3.js/ethers.js)
"""

from web3 import Web3
from web3.contract import Contract
import json
import logging
from typing import Dict, Any, List, Optional, Union
from datetime import datetime
from web3_config import Web3Config

logger = logging.getLogger(__name__)

class ContractService:
    """Service class for smart contract interactions using Web3.py"""
    
    def __init__(self, web3_config: Web3Config):
        self.web3_config = web3_config
        self.web3 = web3_config.get_web3_instance()
        self.contract = web3_config.get_contract_instance()
        self.contract_address = web3_config.get_contract_address()
        self.contract_abi = web3_config.get_contract_abi()
        
        logger.info("ContractService initialized with Web3.py")
    
    def call_read_function(self, function_name: str, params: Dict[str, Any] = None) -> Any:
        """
        Call a read-only contract function
        Equivalent to using web3.js contract.methods.functionName().call()
        """
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            # Get the contract function
            contract_function = getattr(self.contract.functions, function_name)
            
            # Call function with parameters
            if params:
                # Convert parameters to appropriate types
                converted_params = self._convert_parameters(params)
                result = contract_function(**converted_params).call()
            else:
                result = contract_function().call()
            
            # Format result for JSON serialization
            formatted_result = self._format_contract_result(result)
            
            logger.info(f"Read function '{function_name}' executed successfully")
            return formatted_result
            
        except AttributeError:
            raise ValueError(f"Function '{function_name}' not found in contract")
        except Exception as e:
            logger.error(f"Error calling read function '{function_name}': {str(e)}")
            raise
    
    def build_transaction(self, function_name: str, params: Dict[str, Any] = None,
                         from_address: str = None, gas_limit: int = None, 
                         gas_price: int = None) -> Dict[str, Any]:
        """
        Build a transaction for a write function
        Equivalent to using web3.js contract.methods.functionName().buildTransaction()
        """
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            # Get the contract function
            contract_function = getattr(self.contract.functions, function_name)
            
            # Prepare function call
            if params:
                converted_params = self._convert_parameters(params)
                function_call = contract_function(**converted_params)
            else:
                function_call = contract_function()
            
            # Get transaction parameters
            if not gas_price:
                gas_price = self.web3.eth.gas_price
            
            if not gas_limit:
                gas_limit = function_call.estimate_gas({'from': from_address})
                gas_limit = int(gas_limit * 1.2)  # Add 20% buffer
            
            # Build transaction
            transaction = function_call.build_transaction({
                'from': Web3.to_checksum_address(from_address),
                'gas': gas_limit,
                'gasPrice': gas_price,
                'nonce': self.web3.eth.get_transaction_count(from_address)
            })
            
            logger.info(f"Transaction built for function '{function_name}'")
            return transaction
            
        except AttributeError:
            raise ValueError(f"Function '{function_name}' not found in contract")
        except Exception as e:
            logger.error(f"Error building transaction for '{function_name}': {str(e)}")
            raise
    
    def estimate_gas(self, function_name: str, params: Dict[str, Any] = None,
                    from_address: str = None) -> int:
        """
        Estimate gas for a transaction
        Equivalent to using web3.js contract.methods.functionName().estimateGas()
        """
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            contract_function = getattr(self.contract.functions, function_name)
            
            if params:
                converted_params = self._convert_parameters(params)
                gas_estimate = contract_function(**converted_params).estimate_gas({
                    'from': from_address
                })
            else:
                gas_estimate = contract_function().estimate_gas({
                    'from': from_address
                })
            
            logger.info(f"Gas estimated for '{function_name}': {gas_estimate}")
            return gas_estimate
            
        except AttributeError:
            raise ValueError(f"Function '{function_name}' not found in contract")
        except Exception as e:
            logger.error(f"Error estimating gas for '{function_name}': {str(e)}")
            raise
    
    def get_transaction_status(self, tx_hash: str) -> Dict[str, Any]:
        """
        Get transaction status and receipt
        Equivalent to web3.js eth.getTransactionReceipt()
        """
        try:
            # Get transaction details
            tx = self.web3.eth.get_transaction(tx_hash)
            
            # Try to get receipt
            try:
                receipt = self.web3.eth.get_transaction_receipt(tx_hash)
                current_block = self.web3.eth.block_number
                
                return {
                    'status': 'success' if receipt.status == 1 else 'failed',
                    'block_number': receipt.blockNumber,
                    'gas_used': receipt.gasUsed,
                    'effective_gas_price': getattr(receipt, 'effectiveGasPrice', tx.gasPrice),
                    'transaction_fee': receipt.gasUsed * getattr(receipt, 'effectiveGasPrice', tx.gasPrice),
                    'confirmations': current_block - receipt.blockNumber,
                    'transaction_hash': tx_hash,
                    'from_address': tx['from'],
                    'to_address': tx.to,
                    'value': str(tx.value),
                    'logs': [dict(log) for log in receipt.logs] if receipt.logs else []
                }
                
            except Exception:
                # Transaction is still pending
                return {
                    'status': 'pending',
                    'transaction_hash': tx_hash,
                    'from_address': tx['from'],
                    'to_address': tx.to,
                    'value': str(tx.value),
                    'gas_price': str(tx.gasPrice),
                    'gas_limit': tx.gas
                }
                
        except Exception as e:
            logger.error(f"Error getting transaction status: {str(e)}")
            raise
    
    def get_contract_events(self, from_block: Union[str, int] = 'latest',
                           to_block: Union[str, int] = 'latest',
                           event_name: str = None) -> List[Dict[str, Any]]:
        """
        Get contract events
        Equivalent to web3.js contract.getPastEvents()
        """
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            events = []
            
            # Convert block parameters
            if from_block == 'latest':
                from_block = max(0, self.web3.eth.block_number - 1000)  # Last 1000 blocks
            
            if to_block == 'latest':
                to_block = self.web3.eth.block_number
            
            # Get specific event or all events
            if event_name:
                # Get specific event
                event_filter = getattr(self.contract.events, event_name).create_filter(
                    fromBlock=from_block,
                    toBlock=to_block
                )
            else:
                # Get all events (this may not work on all contracts)
                try:
                    event_filter = self.contract.events.get_all_entries(
                        fromBlock=from_block,
                        toBlock=to_block
                    )
                except:
                    # Fallback to getting logs directly
                    logs = self.web3.eth.get_logs({
                        'fromBlock': from_block,
                        'toBlock': to_block,
                        'address': self.contract_address
                    })
                    
                    return [{
                        'transaction_hash': log.transactionHash.hex(),
                        'block_number': log.blockNumber,
                        'log_index': log.logIndex,
                        'data': log.data,
                        'topics': [topic.hex() for topic in log.topics]
                    } for log in logs]
            
            # Process events
            for event in event_filter.get_all_entries():
                events.append({
                    'event_name': event.event,
                    'args': dict(event.args),
                    'transaction_hash': event.transactionHash.hex(),
                    'block_number': event.blockNumber,
                    'log_index': event.logIndex,
                    'address': event.address
                })
            
            logger.info(f"Retrieved {len(events)} events")
            return events
            
        except Exception as e:
            logger.error(f"Error getting contract events: {str(e)}")
            return []
    
    def get_account_balance(self, address: str) -> Dict[str, Any]:
        """Get account balance in ETH and Wei"""
        try:
            balance_wei = self.web3.eth.get_balance(address)
            balance_eth = Web3.from_wei(balance_wei, 'ether')
            
            return {
                'address': address,
                'balance_wei': str(balance_wei),
                'balance_eth': str(balance_eth),
                'balance_formatted': f"{float(balance_eth):.6f} ETH"
            }
            
        except Exception as e:
            logger.error(f"Error getting balance for {address}: {str(e)}")
            raise
    
    def get_network_info(self) -> Dict[str, Any]:
        """Get current network information"""
        try:
            return {
                'chain_id': self.web3.eth.chain_id,
                'network_name': self.web3_config.get_network_name(),
                'block_number': self.web3.eth.block_number,
                'gas_price': str(self.web3.eth.gas_price),
                'gas_price_gwei': Web3.from_wei(self.web3.eth.gas_price, 'gwei'),
                'is_connected': self.web3.is_connected(),
                'node_version': self.web3.client_version if hasattr(self.web3, 'client_version') else 'Unknown'
            }
            
        except Exception as e:
            logger.error(f"Error getting network info: {str(e)}")
            raise
    
    def get_gas_info(self) -> Dict[str, Any]:
        """Get current gas information"""
        try:
            gas_price = self.web3.eth.gas_price
            
            return {
                'gas_price_wei': str(gas_price),
                'gas_price_gwei': str(Web3.from_wei(gas_price, 'gwei')),
                'gas_price_formatted': f"{float(Web3.from_wei(gas_price, 'gwei')):.2f} Gwei"
            }
            
        except Exception as e:
            logger.error(f"Error getting gas info: {str(e)}")
            raise
    
    def get_contract_info(self) -> Dict[str, Any]:
        """Get contract information"""
        try:
            return {
                'address': self.contract_address,
                'abi_functions': len([item for item in self.contract_abi if item['type'] == 'function']),
                'abi_events': len([item for item in self.contract_abi if item['type'] == 'event']),
                'network': self.web3_config.get_network_name()
            }
            
        except Exception as e:
            logger.error(f"
