"""
Application.py - Main Flask application with UI routes
Python equivalent to Application.java
"""

from flask import Flask, render_template, request, jsonify, redirect, url_for, flash
from flask_cors import CORS
import logging
import os
from contract_controller import ContractController
from web3_config import Web3Config

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class Application:
    def __init__(self):
        self.app = Flask(__name__, 
                        template_folder='templates',
                        static_folder='static')
        
        # Configuration
        self.app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'dev-secret-key')
        self.app.config['DEBUG'] = os.environ.get('FLASK_DEBUG', 'False').lower() == 'true'
        
        # Enable CORS
        CORS(self.app)
        
        # Initialize Web3 configuration
        self.web3_config = Web3Config()
        
        # Initialize controller with web3 config
        self.contract_controller = ContractController(self.web3_config)
        
        # Register routes
        self._register_routes()
        
        logger.info("Flask application initialized successfully")
    
    def _register_routes(self):
        """Register all application routes"""
        
        @self.app.route('/')
        def index():
            """Main dashboard page"""
            try:
                network_info = self.contract_controller.get_network_info()
                return render_template('index.html', 
                                     network_info=network_info,
                                     title="Smart Contract Interface")
            except Exception as e:
                logger.error(f"Error loading index page: {str(e)}")
                return render_template('error.html', 
                                     error="Failed to load application"), 500
        
        @self.app.route('/contract')
        def contract_interface():
            """Contract interaction page"""
            try:
                contract_info = self.contract_controller.get_contract_info()
                return render_template('contract.html',
                                     contract_info=contract_info,
                                     title="Contract Interface")
            except Exception as e:
                logger.error(f"Error loading contract page: {str(e)}")
                flash('Error loading contract information', 'error')
                return redirect(url_for('index'))
        
        @self.app.route('/wallet')
        def wallet_page():
            """Wallet connection and management page"""
            return render_template('wallet.html', title="Wallet Management")
        
        @self.app.route('/transactions')
        def transactions_page():
            """Transaction history page"""
            return render_template('transactions.html', title="Transaction History")
        
        # API Routes (delegated to controller)
        @self.app.route('/api/connect', methods=['POST'])
        def connect_wallet():
            """Connect wallet endpoint"""
            return self.contract_controller.connect_wallet(request)
        
        @self.app.route('/api/contract/read/<function_name>')
        def read_contract(function_name):
            """Read from contract"""
            return self.contract_controller.read_contract_function(function_name, request)
        
        @self.app.route('/api/contract/write/<function_name>', methods=['POST'])
        def write_contract(function_name):
            """Write to contract"""
            return self.contract_controller.write_contract_function(function_name, request)
        
        @self.app.route('/api/transaction/<tx_hash>')
        def get_transaction(tx_hash):
            """Get transaction status"""
            return self.contract_controller.get_transaction_status(tx_hash)
        
        @self.app.route('/api/balance/<address>')
        def get_balance(address):
            """Get account balance"""
            return self.contract_controller.get_account_balance(address)
        
        @self.app.route('/api/network')
        def network_info():
            """Get network information"""
            return self.contract_controller.get_network_info()
        
        @self.app.route('/api/contract/events')
        def contract_events():
            """Get contract events"""
            return self.contract_controller.get_contract_events(request)
        
        @self.app.route('/api/gas-price')
        def gas_price():
            """Get current gas price"""
            return self.contract_controller.get_gas_price()
        
        # Error Handlers
        @self.app.errorhandler(404)
        def not_found(error):
            if request.path.startswith('/api/'):
                return jsonify({'error': 'Endpoint not found'}), 404
            return render_template('404.html'), 404
        
        @self.app.errorhandler(500)
        def internal_error(error):
            logger.error(f"Internal server error: {str(error)}")
            if request.path.startswith('/api/'):
                return jsonify({'error': 'Internal server error'}), 500
            return render_template('500.html'), 500
        
        @self.app.errorhandler(Exception)
        def handle_exception(e):
            logger.error(f"Unhandled exception: {str(e)}")
            if request.path.startswith('/api/'):
                return jsonify({'error': 'An unexpected error occurred'}), 500
            return render_template('error.html', error=str(e)), 500
        
        # Template filters
        @self.app.template_filter('truncate_hash')
        def truncate_hash(hash_string, length=10):
            """Truncate hash for display"""
            if not hash_string:
                return ""
            if len(hash_string) <= length * 2:
                return hash_string
            return f"{hash_string[:length]}...{hash_string[-length:]}"
        
        @self.app.template_filter('wei_to_ether')
        def wei_to_ether(wei_amount):
            """Convert wei to ether"""
            try:
                from web3 import Web3
                return Web3.from_wei(int(wei_amount), 'ether')
            except:
                return "0"
    
    def run(self, host='127.0.0.1', port=5000, debug=None):
        """Run the Flask application"""
        if debug is None:
            debug = self.app.config['DEBUG']
        
        logger.info(f"Starting Flask application on {host}:{port}")
        logger.info(f"Debug mode: {debug}")
        
        self.app.run(host=host, port=port, debug=debug)
    
    def create_app(self):
        """Factory method to create Flask app"""
        return self.app

# Application factory function
def create_app():
    """Create and configure Flask application"""
    app_instance = Application()
    return app_instance.create_app()

# Main execution
if __name__ == '__main__':
    app = Application()
    
    # Get configuration from environment
    host = os.environ.get('FLASK_HOST', '127.0.0.1')
    port = int(os.environ.get('FLASK_PORT', 5000))
    debug = os.environ.get('FLASK_DEBUG', 'False').lower() == 'true'
    
    # Run application
    app.run(host=host, port=port, debug=debug)
