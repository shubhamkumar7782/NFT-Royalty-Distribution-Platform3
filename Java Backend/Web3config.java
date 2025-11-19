
"""
web3_config.py - Web3 Configuration and Connection Management
Python equivalent to Web3Config.java - handles Web3.py setup (Python's web3.js/ethers.js equivalent)
"""

from web3 import Web3
from web3.middleware import geth_poa_middleware
from web3.contract import Contract
import json
import os
import logging
from typing import Dict, Any, Optional, List

logger = logging.getLogger(__name__)

class Web3Config:
    """Configuration class for Web3.py connection and contract setup"""
    
    def __init__(self):
        """Initialize Web3 configuration"""
        self.web3 = None
        self.contract = None
        self.contract_address = None
        self.contract_abi = None
        
        # Load configuration from environment
        self._load_config()
        
        # Initialize Web3 connection
        self._initialize_web3()
        
        # Load and initialize contract
        self._initialize_contract()
        
        logger.info("Web3Config initialized successfully")
    
    def _load_config(self):
        """Load configuration from environment variables"""
        # Network configuration
        self.network_name = os.getenv('NETWORK_NAME', 'sepolia')
        self.rpc_url = os.getenv('RPC_URL', 'https://sepolia.infura.io/v3/YOUR_INFURA_KEY')
        self.chain_id = int(os.getenv('CHAIN_ID', '11155111'))  # Sepolia default
        
        # Contract configuration
        self.contract_address = os.getenv('CONTRACT_ADDRESS', '')
        self.contract_abi_path = os.getenv('CONTRACT_ABI_PATH', 'contracts/abi.json')
        
        # Account configuration (for server-side transactions)
        self.private_key = os.getenv('PRIVATE_KEY', '')
        self.default_account = os.getenv('DEFAULT_ACCOUNT', '')
        
        # Gas configuration
        self.default_gas_limit = int(os.getenv('DEFAULT_GAS_LIMIT', '3000000'))
        self.max_gas_price = int(os.getenv('MAX_GAS_PRICE', '50'))  # in gwei
        self.gas_price_strategy = os.getenv('GAS_PRICE_STRATEGY', 'medium')
        
        # Connection configuration
        self.connection_timeout = int(os.getenv('CONNECTION_TIMEOUT', '30'))
        self.request_timeout = int(os.getenv('REQUEST_TIMEOUT', '60'))
        
        logger.info(f"Configuration loaded for network: {self.network_name}")
    
    def _initialize_web3(self):
        """Initialize Web3 connection"""
        try:
            # Create Web3 instance
            if self.rpc_url.startswith('wss://'):
                # WebSocket connection
                from web3 import WebsocketProvider
                provider = WebsocketProvider(self.rpc_url)
            elif self.rpc_url.startswith('http'):
                # HTTP connection
                from web3 import HTTPProvider
                provider = HTTPProvider(
                    self.rpc_url,
                    request_kwargs={'timeout': self.request_timeout}
                )
            else:
                raise ValueError(f"Unsupported RPC URL format: {self.rpc_url}")
            
            self.web3 = Web3(provider)
            
            # Add middleware for PoA networks (like Polygon, BSC)
            if self.network_name.lower() in ['polygon', 'mumbai', 'bsc', 'bsc-testnet', 'fantom']:
                self.web3.middleware_onion.inject(geth_poa_middleware, layer=0)
                logger.info("PoA middleware added for network")
            
            # Set default account if provided
            if self.private_key and self.default_account:
                account = self.web3.eth.account.from_key(self.private_key)
                self.web3.eth.default_account = account.address
                logger.info(f"Default account set: {account.address}")
            
            # Test connection
            if self.web3.is_connected():
                latest_block = self.web3.eth.block_number
                logger.info(f"Connected to {self.network_name} - Latest block: {latest_block}")
                logger.info(f"Chain ID: {self.web3.eth.chain_id}")
            else:
                raise Exception(f"Failed to connect to {self.network_name}")
                
        except Exception as e:
            logger.error(f"Web3 initialization failed: {str(e)}")
            raise
    
    def _initialize_contract(self):
        """Load and initialize smart contract"""
        try:
            if not self.contract_address:
                logger.warning("No contract address provided - contract functions will not be available")
                return
            
            # Load contract ABI
            self._load_contract_abi()
            
            if not self.contract_abi:
                logger.warning("No contract ABI loaded - contract functions will not be available")
                return
            
            # Create contract instance
            self.contract = self.web3.eth.contract(
                address=Web3.to_checksum_address(self.contract_address),
                abi=self.contract_abi
            )
            
            logger.info(f"Contract initialized: {self.contract_address}")
            
            # Log available functions
            functions = [item['name'] for item in self.contract_abi if item['type'] == 'function']
            logger.info(f"Available functions: {', '.join(functions)}")
            
        except Exception as e:
            logger.error(f"Contract initialization failed: {str(e)}")
            raise
    
    def _load_contract_abi(self):
        """Load contract ABI from file or environment"""
        try:
            # Try to load from environment variable first
            abi_json = os.getenv('CONTRACT_ABI')
            if abi_json:
                self.contract_abi = json.loads(abi_json)
                logger.info("Contract ABI loaded from environment variable")
                return
            
            # Load from file
            if os.path.exists(self.contract_abi_path):
                with open(self.contract_abi_path, 'r') as f:
                    self.contract_abi = json.load(f)
                logger.info(f"Contract ABI loaded from file: {self.contract_abi_path}")
            else:
                logger.warning(f"Contract ABI file not found: {self.contract_abi_path}")
                
        except Exception as e:
            logger.error(f"Error loading contract ABI: {str(e)}")
            raise
    
    def get_web3_instance(self) -> Web3:
        """Get Web3 instance"""
        return self.web3
    
    def get_contract_instance(self) -> Optional[Contract]:
        """Get contract instance"""
        return self.contract
    
    def get_contract_address(self) -> str:
        """Get contract address"""
        return self.contract_address
    
    def get_contract_abi(self) -> List[Dict[str, Any]]:
        """Get contract ABI"""
        return self.contract_abi
    
    def get_network_name(self) -> str:
        """Get network name"""
        return self.network_name
    
    def get_chain_id(self) -> int:
        """Get chain ID"""
        return self.web3.eth.chain_id if self.web3 else self.chain_id
    
    def is_connected(self) -> bool:
        """Check if Web3 is connected"""
        return self.web3.is_connected() if self.web3 else False
    
    def get_network_config(self) -> Dict[str, Any]:
        """Get comprehensive network configuration"""
        return {
            'network_name': self.network_name,
            'rpc_url': self.rpc_url,
            'chain_id': self.get_chain_id(),
            'contract_address': self.contract_address,
            'default_gas_limit': self.default_gas_limit,
            'max_gas_price': self.max_gas_price,
            'is_connected': self.is_connected()
        }
    
    def get_gas_strategy(self) -> Dict[str, int]:
        """Get gas price strategy in gwei"""
        try:
            current_gas_price = self.web3.eth.gas_price
            current_gwei = Web3.from_wei(current_gas_price, 'gwei')
            
            # Define gas price strategies
            strategies = {
                'slow': max(1, int(current_gwei * 0.8)),
                'standard': int(current_gwei),
                'fast': int(current_gwei * 1.2),
                'fastest': min(self.max_gas_price, int(current_gwei * 1.5))
            }
            
            return strategies
            
        except Exception as e:
            logger.error(f"Error getting gas strategy: {str(e)}")
            # Fallback values in gwei
            return {
                'slow': 10,
                'standard': 20,
                'fast': 30,
                'fastest': 50
            }
    
    def estimate_transaction_cost(self, gas_limit: int, gas_price_gwei: int = None) -> Dict[str, Any]:
        """Estimate transaction cost"""
        try:
            if not gas_price_gwei:
                gas_price_gwei = int(Web3.from_wei(self.web3.eth.gas_price, 'gwei'))
            
            gas_price_wei = Web3.to_wei(gas_price_gwei, 'gwei')
            total_cost_wei = gas_limit * gas_price_wei
            total_cost_eth = Web3.from_wei(total_cost_wei, 'ether')
            
            return {
                'gas_limit': gas_limit,
                'gas_price_gwei': gas_price_gwei,
                'gas_price_wei': str(gas_price_wei),
                'total_cost_wei': str(total_cost_wei),
                'total_cost_eth': str(total_cost_eth),
                'total_cost_formatted': f"{float(total_cost_eth):.8f} ETH"
            }
            
        except Exception as e:
            logger.error(f"Error estimating transaction cost: {str(e)}")
            raise
    
    def get_block_info(self, block_number: str = 'latest') -> Dict[str, Any]:
        """Get block information"""
        try:
            block = self.web3.eth.get_block(block_number)
            
            return {
                'number': block.number,
                'hash': block.hash.hex(),
                'timestamp': block.timestamp,
                'gas_limit': block.gasLimit,
                'gas_used': block.gasUsed,
                'gas_used_percentage': (block.gasUsed / block.gasLimit) * 100,
                'transaction_count': len(block.transactions),
                'miner': block.miner if hasattr(block, 'miner') else 'N/A'
            }
            
        except Exception as e:
            logger.error(f"Error getting block info: {str(e)}")
            raise
    
    def create_account(self) -> Dict[str, str]:
        """Create a new Ethereum account"""
        try:
            account = self.web3.eth.account.create()
            
            return {
                'address': account.address,
                'private_key': account.privateKey.hex(),
                'warning': 'Store private key securely and never share it!'
            }
            
        except Exception as e:
            logger.error(f"Error creating account: {str(e)}")
            raise
    
    def validate_configuration(self) -> Dict[str, Any]:
        """Validate current configuration"""
        validation_results = {
            'web3_connected': self.is_connected(),
            'contract_loaded': self.contract is not None,
            'abi_loaded': self.contract_abi is not None,
            'valid_network': False,
            'errors': []
        }
        
        try:
            # Validate network connection
            if self.is_connected():
                chain_id = self.web3.eth.chain_id
                validation_results['actual_chain_id'] = chain_id
                validation_results['expected_chain_id'] = self.chain_id
                validation_results['valid_network'] = chain_id == self.chain_id
                
                if chain_id != self.chain_id:
                    validation_results['errors'].append(
                        f"Chain ID mismatch: expected {self.chain_id}, got {chain_id}"
                    )
            else:
                validation_results['errors'].append("Web3 not connected")
            
            # Validate contract
            if not self.contract_address:
                validation_results['errors'].append("No contract address provided")
            
            if not self.contract_abi:
                validation_results['errors'].append("No contract ABI loaded")
            
        except Exception as e:
            validation_results['errors'].append(f"Validation error: {str(e)}")
        
        return validation_results
