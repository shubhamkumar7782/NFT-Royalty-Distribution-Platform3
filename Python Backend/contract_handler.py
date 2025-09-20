from web3 import Web3
from web3.middleware import geth_poa_middleware
import json
import os
import logging
from typing import Dict, Any, List, Optional
from config import Config

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class ContractHandler:
    """Handle smart contract interactions using Web3.py"""
    
    def __init__(self):
        self.config = Config()
        self.web3 = None
        self.contract = None
        self.contract_abi = None
        self.initialize_web3()
        self.load_contract()
    
    def initialize_web3(self):
        """Initialize Web3 connection"""
        try:
            # Connect to blockchain network
            self.web3 = Web3(Web3.HTTPProvider(self.config.RPC_URL))
            
            # Add PoA middleware for networks like Polygon
            if self.config.BLOCKCHAIN_NETWORK in ['polygon', 'mumbai', 'bsc']:
                self.web3.middleware_onion.inject(geth_poa_middleware, layer=0)
            
            # Check connection
            if self.web3.is_connected():
                logger.info(f"Connected to {self.config.BLOCKCHAIN_NETWORK} network")
                logger.info(f"Latest block: {self.web3.eth.block_number}")
            else:
                logger.error("Failed to connect to blockchain network")
                
        except Exception as e:
            logger.error(f"Web3 initialization error: {str(e)}")
            raise
    
    def load_contract(self):
        """Load contract ABI and create contract instance"""
        try:
            if not self.config.CONTRACT_ADDRESS:
                logger.warning("No contract address provided")
                return
            
            # Load contract ABI
            if os.path.exists(self.config.CONTRACT_ABI_PATH):
                with open(self.config.CONTRACT_ABI_PATH, 'r') as f:
                    self.contract_abi = json.load(f)
            else:
                logger.error(f"Contract ABI file not found: {self.config.CONTRACT_ABI_PATH}")
                return
            
            # Create contract instance
            self.contract = self.web3.eth.contract(
                address=Web3.to_checksum_address(self.config.CONTRACT_ADDRESS),
                abi=self.contract_abi
            )
            logger.info("Contract loaded successfully")
            
        except Exception as e:
            logger.error(f"Contract loading error: {str(e)}")
            raise
    
    def call_read_function(self, function_name: str, params: Dict[str, Any] = None) -> Any:
        """Call a read-only contract function"""
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            contract_function = getattr(self.contract.functions, function_name)
            
            if params:
                # Convert parameters and call function
                result = contract_function(**params).call()
            else:
                result = contract_function().call()
            
            logger.info(f"Read function {function_name} called successfully")
            return self._format_result(result)
            
        except Exception as e:
            logger.error(f"Error calling read function {function_name}: {str(e)}")
            raise
    
    def call_write_function(self, function_name: str, params: Dict[str, Any], 
                          account_address: str, gas_limit: int = None) -> str:
        """Call a write contract function (requires transaction)"""
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            contract_function = getattr(self.contract.functions, function_name)
            
            # Build transaction
            if params:
                transaction = contract_function(**params)
            else:
                transaction = contract_function()
            
            # Estimate gas if not provided
            if not gas_limit:
                gas_limit = transaction.estimate_gas({'from': account_address})
                gas_limit = int(gas_limit * 1.2)  # Add 20% buffer
            
            # Build transaction dict
            tx_dict = transaction.build_transaction({
                'from': account_address,
                'gas': gas_limit,
                'gasPrice': self.web3.to_wei(self.config.MAX_GAS_PRICE, 'gwei'),
                'nonce': self.web3.eth.get_transaction_count(account_address)
            })
            
            # For server-side signing (if private key is available)
            if self.config.PRIVATE_KEY:
                signed_txn = self.web3.eth.account.sign_transaction(
                    tx_dict, private_key=self.config.PRIVATE_KEY
                )
                tx_hash = self.web3.eth.send_raw_transaction(signed_txn.rawTransaction)
                return tx_hash.hex()
            else:
                # Return transaction dict for frontend signing
                return tx_dict
                
        except Exception as e:
            logger.error(f"Error calling write function {function_name}: {str(e)}")
            raise
    
    def get_transaction_status(self, tx_hash: str) -> Dict[str, Any]:
        """Get transaction status and receipt"""
        try:
            # Get transaction
            tx = self.web3.eth.get_transaction(tx_hash)
            
            # Try to get receipt
            try:
                receipt = self.web3.eth.get_transaction_receipt(tx_hash)
                status = "success" if receipt.status == 1 else "failed"
                return {
                    'status': status,
                    'block_number': receipt.blockNumber,
                    'gas_used': receipt.gasUsed,
                    'transaction_hash': tx_hash,
                    'confirmations': self.web3.eth.block_number - receipt.blockNumber
                }
            except:
                # Transaction is pending
                return {
                    'status': 'pending',
                    'transaction_hash': tx_hash,
                    'block_number': None,
                    'gas_used': None,
                    'confirmations': 0
                }
                
        except Exception as e:
            logger.error(f"Error getting transaction status: {str(e)}")
            raise
    
    def get_contract_events(self, from_block: str = 'latest', to_block: str = 'latest') -> List[Dict]:
        """Get contract events"""
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            # Get all events for the contract
            events = []
            
            if from_block == 'latest':
                from_block = self.web3.eth.block_number - 100  # Last 100 blocks
            
            if to_block == 'latest':
                to_block = self.web3.eth.block_number
            
            # Get events (this is a general approach - you might want to filter specific events)
            event_filter = self.contract.events.getAllEvents.create_filter(
                fromBlock=from_block,
                toBlock=to_block
            )
            
            for event in event_filter.get_all_entries():
                events.append({
                    'event': event.event,
                    'args': dict(event.args),
                    'transaction_hash': event.transactionHash.hex(),
                    'block_number': event.blockNumber,
                    'log_index': event.logIndex
                })
            
            return events
            
        except Exception as e:
            logger.error(f"Error getting contract events: {str(e)}")
            return []
    
    def get_network_info(self) -> Dict[str, Any]:
        """Get current network information"""
        try:
            return {
                'network': self.config.BLOCKCHAIN_NETWORK,
                'chain_id': self.web3.eth.chain_id,
                'block_number': self.web3.eth.block_number,
                'gas_price': self.web3.eth.gas_price,
                'is_connected': self.web3.is_connected(),
                'contract_address': self.config.CONTRACT_ADDRESS
            }
        except Exception as e:
            logger.error(f"Error getting network info: {str(e)}")
            raise
    
    def get_account_balance(self, account_address: str) -> Dict[str, Any]:
        """Get account ETH balance"""
        try:
            balance_wei = self.web3.eth.get_balance(account_address)
            balance_eth = self.web3.from_wei(balance_wei, 'ether')
            
            return {
                'address': account_address,
                'balance_wei': str(balance_wei),
                'balance_eth': str(balance_eth),
                'balance_formatted': f"{balance_eth:.4f} ETH"
            }
        except Exception as e:
            logger.error(f"Error getting account balance: {str(e)}")
            raise
    
    def estimate_gas(self, function_name: str, params: Dict[str, Any], 
                    account_address: str) -> int:
        """Estimate gas for a transaction"""
        if not self.contract:
            raise Exception("Contract not initialized")
        
        try:
            contract_function = getattr(self.contract.functions, function_name)
            
            if params:
                gas_estimate = contract_function(**params).estimate_gas({'from': account_address})
            else:
                gas_estimate = contract_function().estimate_gas({'from': account_address})
            
            return gas_estimate
            
        except Exception as e:
            logger.error(f"Error estimating gas: {str(e)}")
            raise
    
    def _format_result(self, result) -> Any:
        """Format contract call result for JSON serialization"""
        if isinstance(result, (list, tuple)):
            return [self._format_result(item) for item in result]
        elif hasattr(result, '__dict__'):
            return {k: self._format_result(v) for k, v in result.__dict__.items()}
        elif isinstance(result, bytes):
            return result.hex()
        else:
            return result
    
    def is_valid_address(self, address: str) -> bool:
        """Check if address is a valid Ethereum address"""
        try:
            Web3.to_checksum_address(address)
            return True
        except:
            return False
