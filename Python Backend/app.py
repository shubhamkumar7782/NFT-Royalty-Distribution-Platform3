from flask import Flask, render_template, request, jsonify
from flask_cors import CORS
import json
import os
from contract_handler import ContractHandler
from config import Config

app = Flask(__name__)
CORS(app)  # Enable CORS for frontend communication
app.config.from_object(Config)

# Initialize contract handler
contract_handler = ContractHandler()

@app.route('/')
def index():
    """Main page with contract interaction UI"""
    return render_template('index.html')

@app.route('/api/connect', methods=['POST'])
def connect_wallet():
    """Connect to wallet and get account info"""
    try:
        account_address = request.json.get('account_address')
        if account_address:
            # Store or validate the account address
            return jsonify({
                'success': True, 
                'message': 'Wallet connected successfully',
                'account': account_address
            })
        else:
            return jsonify({'success': False, 'message': 'No account address provided'})
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.route('/api/contract/read/<function_name>', methods=['GET'])
def read_contract(function_name):
    """Read data from smart contract"""
    try:
        # Get any parameters from query string
        params = dict(request.args)
        result = contract_handler.call_read_function(function_name, params)
        return jsonify({'success': True, 'result': result})
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.route('/api/contract/write/<function_name>', methods=['POST'])
def write_contract(function_name):
    """Write data to smart contract"""
    try:
        data = request.json
        account_address = data.get('account_address')
        params = data.get('params', {})
        
        if not account_address:
            return jsonify({'success': False, 'message': 'Account address required'})
        
        tx_hash = contract_handler.call_write_function(
            function_name, 
            params, 
            account_address
        )
        
        return jsonify({
            'success': True, 
            'tx_hash': tx_hash,
            'message': 'Transaction sent successfully'
        })
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.route('/api/transaction/<tx_hash>', methods=['GET'])
def get_transaction_status(tx_hash):
    """Get transaction status"""
    try:
        status = contract_handler.get_transaction_status(tx_hash)
        return jsonify({'success': True, 'status': status})
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.route('/api/contract/events', methods=['GET'])
def get_contract_events():
    """Get recent contract events"""
    try:
        from_block = request.args.get('from_block', 'latest')
        events = contract_handler.get_contract_events(from_block)
        return jsonify({'success': True, 'events': events})
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.route('/api/network', methods=['GET'])
def get_network_info():
    """Get current network information"""
    try:
        network_info = contract_handler.get_network_info()
        return jsonify({'success': True, 'network': network_info})
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.errorhandler(404)
def not_found(error):
    return jsonify({'success': False, 'message': 'Endpoint not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({'success': False, 'message': 'Internal server error'}), 500

if __name__ == '__main__':
    app.run(
        debug=app.config['DEBUG'],
        host=app.config['HOST'],
        port=app.config['PORT']
    )
