
"""
contract_controller.py - REST API Controller for Smart Contract Interactions
Python equivalent to ContractController.java
"""

from flask import jsonify, request
import logging
from typing import Dict, Any, Optional
from contract_service import ContractService
from web3_config import Web3Config

logger = logging.getLogger(__name__)

class ContractController:
    """Controller class handling HTTP requests for smart contract operations"""
    
    def __init__(self, web3_config: Web3Config):
        self.web3_config = web3_config
        self.contract_service = ContractService(web3_config)
        logger.info("ContractController initialized")
    
    def connect_wallet(self, request) -> Dict[str, Any]:
        """Handle wallet connection requests"""
        try:
            data = request.get_json()
            
            if not data:
                return jsonify({
                    'success': False,
                    'error': 'No data provided'
                }), 400
            
            wallet_address = data.get('address')
            
            if not wallet_address:
                return jsonify({
                    'success': False,
                    'error': 'Wallet address is required'
                }), 400
            
            # Validate wallet address
            if not self.contract_service.is_valid_address(wallet_address):
                return jsonify({
                    'success': False,
                    'error': 'Invalid wallet address'
                }), 400
            
            # Get wallet information
            wallet_info = self.contract_service.get_wallet_info(wallet_address)
            
            logger.info(f"Wallet connected: {wallet_address}")
            
            return jsonify({
                'success': True,
                'message': 'Wallet connected successfully',
                'data': wallet_info
            })
            
        except Exception as e:
            logger.error(f"Error connecting wallet: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to connect wallet'
            }), 500
    
    def read_contract_function(self, function_name: str, request) -> Dict[str, Any]:
        """Handle contract read function calls"""
        try:
            # Get parameters from query string
            params = {}
            for key, value in request.args.items():
                params[key] = value
            
            logger.info(f"Reading contract function: {function_name} with params: {params}")
            
            # Call contract service
            result = self.contract_service.call_read_function(function_name, params)
            
            return jsonify({
                'success': True,
                'function_name': function_name,
                'result': result,
                'timestamp': self.contract_service.get_current_timestamp()
            })
            
        except ValueError as e:
            logger.warning(f"Invalid parameters for function {function_name}: {str(e)}")
            return jsonify({
                'success': False,
                'error': f'Invalid parameters: {str(e)}'
            }), 400
            
        except Exception as e:
            logger.error(f"Error reading contract function {function_name}: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to read contract function'
            }), 500
    
    def write_contract_function(self, function_name: str, request) -> Dict[str, Any]:
        """Handle contract write function calls"""
        try:
            data = request.get_json()
            
            if not data:
                return jsonify({
                    'success': False,
                    'error': 'No data provided'
                }), 400
            
            wallet_address = data.get('from_address')
            params = data.get('params', {})
            gas_limit = data.get('gas_limit')
            gas_price = data.get('gas_price')
            
            if not wallet_address:
                return jsonify({
                    'success': False,
                    'error': 'Wallet address (from_address) is required'
                }), 400
            
            if not self.contract_service.is_valid_address(wallet_address):
                return jsonify({
                    'success': False,
                    'error': 'Invalid wallet address'
                }), 400
            
            logger.info(f"Writing to contract function: {function_name} from {wallet_address}")
            
            # Estimate gas if not provided
            if not gas_limit:
                gas_limit = self.contract_service.estimate_gas(
                    function_name, params, wallet_address
                )
            
            # Build transaction
            transaction = self.contract_service.build_transaction(
                function_name=function_name,
                params=params,
                from_address=wallet_address,
                gas_limit=gas_limit,
                gas_price=gas_price
            )
            
            return jsonify({
                'success': True,
                'function_name': function_name,
                'transaction': transaction,
                'estimated_gas': gas_limit,
                'message': 'Transaction prepared successfully'
            })
            
        except ValueError as e:
            logger.warning(f"Invalid parameters for function {function_name}: {str(e)}")
            return jsonify({
                'success': False,
                'error': f'Invalid parameters: {str(e)}'
            }), 400
            
        except Exception as e:
            logger.error(f"Error writing to contract function {function_name}: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to prepare transaction'
            }), 500
    
    def get_transaction_status(self, tx_hash: str) -> Dict[str, Any]:
        """Get transaction status and receipt"""
        try:
            if not tx_hash:
                return jsonify({
                    'success': False,
                    'error': 'Transaction hash is required'
                }), 400
            
            status = self.contract_service.get_transaction_status(tx_hash)
            
            return jsonify({
                'success': True,
                'transaction_hash': tx_hash,
                'status': status
            })
            
        except Exception as e:
            logger.error(f"Error getting transaction status: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to get transaction status'
            }), 500
    
    def get_account_balance(self, address: str) -> Dict[str, Any]:
        """Get account balance"""
        try:
            if not self.contract_service.is_valid_address(address):
                return jsonify({
                    'success': False,
                    'error': 'Invalid address'
                }), 400
            
            balance_info = self.contract_service.get_account_balance(address)
            
            return jsonify({
                'success': True,
                'address': address,
                'balance': balance_info
            })
            
        except Exception as e:
            logger.error(f"Error getting account balance: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to get account balance'
            }), 500
    
    def get_network_info(self) -> Dict[str, Any]:
        """Get current network information"""
        try:
            network_info = self.contract_service.get_network_info()
            
            return jsonify({
                'success': True,
                'network': network_info
            })
            
        except Exception as e:
            logger.error(f"Error getting network info: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to get network information'
            }), 500
    
    def get_contract_events(self, request) -> Dict[str, Any]:
        """Get contract events"""
        try:
            from_block = request.args.get('from_block', 'latest')
            to_block = request.args.get('to_block', 'latest')
            event_name = request.args.get('event_name')
            
            events = self.contract_service.get_contract_events(
                from_block=from_block,
                to_block=to_block,
                event_name=event_name
            )
            
            return jsonify({
                'success': True,
                'events': events,
                'count': len(events)
            })
            
        except Exception as e:
            logger.error(f"Error getting contract events: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to get contract events'
            }), 500
    
    def get_gas_price(self) -> Dict[str, Any]:
        """Get current gas price"""
        try:
            gas_info = self.contract_service.get_gas_info()
            
            return jsonify({
                'success': True,
                'gas_info': gas_info
            })
            
        except Exception as e:
            logger.error(f"Error getting gas price: {str(e)}")
            return jsonify({
                'success': False,
                'error': 'Failed to get gas price'
            }), 500
    
    def get_contract_info(self) -> Dict[str, Any]:
        """Get contract information for UI"""
        try:
            contract_info = self.contract_service.get_contract_info()
            network_info = self.contract_service.get_network_info()
            
            return {
                'contract': contract_info,
                'network': network_info,
                'available_functions': self.contract_service.get_available_functions()
            }
            
        except Exception as e:
            logger.error(f"Error getting contract info: {str(e)}")
            return {
                'error': 'Failed to load contract information',
                'contract': None,
                'network': None,
                'available_functions': []
            }
    
    def health_check(self) -> Dict[str, Any]:
        """Health check endpoint"""
        try:
            is_connected = self.contract_service.is_connected()
            
            return jsonify({
                'success': True,
                'status': 'healthy' if is_connected else 'unhealthy',
                'web3_connected': is_connected,
                'timestamp': self.contract_service.get_current_timestamp()
            })
            
        except Exception as e:
            logger.error(f"Health check failed: {str(e)}")
            return jsonify({
                'success': False,
                'status': 'unhealthy',
                'error': str(e)
            }), 500
