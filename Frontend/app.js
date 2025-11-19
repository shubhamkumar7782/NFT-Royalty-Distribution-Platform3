
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Smart Contract DApp</title>
    <link rel="stylesheet" href="style.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/ethers/6.7.1/ethers.umd.min.js"></script>
</head>
<body>
    <div class="container">
        <header>
            <h1>Smart Contract DApp</h1>
            <div class="wallet-section">
                <button id="connectWallet" class="btn-primary">Connect Wallet</button>
                <div id="walletInfo" class="wallet-info hidden">
                    <span id="accountAddress"></span>
                    <span id="balance"></span>
                </div>
            </div>
        </header>

        <main>
            <div class="card">
                <h2>Contract Information</h2>
                <div class="info-grid">
                    <div class="info-item">
                        <label>Contract Address:</label>
                        <input type="text" id="contractAddress" placeholder="0x..." />
                    </div>
                    <div class="info-item">
                        <label>Network:</label>
                        <span id="networkName">Not Connected</span>
                    </div>
                </div>
                <button id="connectContract" class="btn-secondary">Connect to Contract</button>
            </div>

            <div class="card">
                <h2>Read Contract</h2>
                <div class="function-section">
                    <div class="input-group">
                        <label for="readFunction">Function Name:</label>
                        <input type="text" id="readFunction" placeholder="e.g., totalSupply, balanceOf" />
                    </div>
                    <div class="input-group">
                        <label for="readParams">Parameters (comma-separated):</label>
                        <input type="text" id="readParams" placeholder="e.g., 0x123..., 100" />
                    </div>
                    <button id="readContract" class="btn-secondary">Read</button>
                    <div id="readResult" class="result"></div>
                </div>
            </div>

            <div class="card">
                <h2>Write Contract</h2>
                <div class="function-section">
                    <div class="input-group">
                        <label for="writeFunction">Function Name:</label>
                        <input type="text" id="writeFunction" placeholder="e.g., transfer, approve" />
                    </div>
                    <div class="input-group">
                        <label for="writeParams">Parameters (comma-separated):</label>
                        <input type="text" id="writeParams" placeholder="e.g., 0x123..., 100" />
                    </div>
                    <div class="input-group">
                        <label for="ethValue">ETH Value (optional):</label>
                        <input type="text" id="ethValue" placeholder="0.0" />
                    </div>
                    <button id="writeContract" class="btn-primary">Execute Transaction</button>
                    <div id="writeResult" class="result"></div>
                </div>
            </div>

            <div class="card">
                <h2>Transaction History</h2>
                <div id="transactionHistory" class="transaction-list">
                    <p class="no-transactions">No transactions yet</p>
                </div>
            </div>
        </main>

        <footer>
            <p>Make sure you're on the correct network and have sufficient gas fees</p>
        </footer>
    </div>

    <!-- Loading Overlay -->
    <div id="loadingOverlay" class="loading-overlay hidden">
        <div class="loading-spinner"></div>
        <p>Processing transaction...</p>
    </div>

    <!-- Notification -->
    <div id="notification" class="notification hidden">
        <span id="notificationMessage"></span>
        <button id="closeNotification">&times;</button>
    </div>

    <script src="app.js"></script>
</body>
</html>

<!-- style.css -->
<style>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    line-height: 1.6;
    color: #333;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

header {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 15px;
    padding: 20px;
    margin-bottom: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

h1 {
    color: #2c3e50;
    font-size: 2.5em;
    font-weight: 700;
}

h2 {
    color: #2c3e50;
    margin-bottom: 20px;
    font-size: 1.8em;
    font-weight: 600;
}

.wallet-section {
    display: flex;
    align-items: center;
    gap: 15px;
}

.wallet-info {
    background: rgba(52, 152, 219, 0.1);
    padding: 10px 15px;
    border-radius: 8px;
    border: 1px solid rgba(52, 152, 219, 0.3);
}

.wallet-info span {
    display: block;
    font-size: 0.9em;
    color: #2980b9;
}

.btn-primary {
    background: linear-gradient(45deg, #3498db, #2980b9);
    color: white;
    padding: 12px 24px;
    border: none;
    border-radius: 8px;
    font-size: 1em;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(52, 152, 219, 0.3);
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

.btn-secondary {
    background: linear-gradient(45deg, #95a5a6, #7f8c8d);
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 6px;
    font-size: 0.9em;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 3px 10px rgba(149, 165, 166, 0.3);
}

.btn-secondary:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 15px rgba(149, 165, 166, 0.4);
}

.btn-primary:disabled,
.btn-secondary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

.card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 15px;
    padding: 25px;
    margin-bottom: 25px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
}

.info-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.info-item label {
    font-weight: 600;
    color: #555;
    font-size: 0.9em;
}

.input-group {
    margin-bottom: 20px;
}

.input-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #555;
}

input[type="text"] {
    width: 100%;
    padding: 12px 16px;
    border: 2px solid #e0e6ed;
    border-radius: 8px;
    font-size: 1em;
    transition: border-color 0.3s ease, box-shadow 0.3s ease;
    background: rgba(255, 255, 255, 0.9);
}

input[type="text"]:focus {
    outline: none;
    border-color: #3498db;
    box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.result {
    margin-top: 15px;
    padding: 15px;
    border-radius: 8px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
    min-height: 20px;
    word-break: break-all;
}

.result.success {
    background: rgba(46, 204, 113, 0.1);
    border: 1px solid rgba(46, 204, 113, 0.3);
    color: #27ae60;
}

.result.error {
    background: rgba(231, 76, 60, 0.1);
    border: 1px solid rgba(231, 76, 60, 0.3);
    color: #e74c3c;
}

.result.info {
    background: rgba(52, 152, 219, 0.1);
    border: 1px solid rgba(52, 152, 219, 0.3);
    color: #2980b9;
}

.transaction-list {
    max-height: 300px;
    overflow-y: auto;
}

.transaction-item {
    background: rgba(236, 240, 241, 0.5);
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    border-left: 4px solid #3498db;
}

.transaction-item h4 {
    color: #2c3e50;
    margin-bottom: 5px;
}

.transaction-item p {
    font-size: 0.9em;
    color: #7f8c8d;
    margin: 2px 0;
}

.no-transactions {
    text-align: center;
    color: #95a5a6;
    font-style: italic;
    padding: 20px;
}

.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 4px solid rgba(255, 255, 255, 0.3);
    border-top: 4px solid #3498db;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20px;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-overlay p {
    color: white;
    font-size: 1.1em;
}

.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: #2c3e50;
    color: white;
    padding: 15px 20px;
    border-radius: 8px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    z-index: 1001;
    display: flex;
    align-items: center;
    gap: 10px;
    max-width: 400px;
}

.notification.success {
    background: #27ae60;
}

.notification.error {
    background: #e74c3c;
}

.notification button {
    background: none;
    border: none;
    color: white;
    font-size: 1.2em;
    cursor: pointer;
    padding: 0;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.hidden {
    display: none !important;
}

footer {
    text-align: center;
    margin-top: 40px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
    .container {
        padding: 10px;
    }
    
    header {
        flex-direction: column;
        gap: 15px;
        text-align: center;
    }
    
    h1 {
        font-size: 2em;
    }
    
    .info-grid {
        grid-template-columns: 1fr;
    }
    
    .wallet-info span {
        font-size: 0.8em;
    }
}
</style>

<!-- app.js -->
<script>
// Global variables
let provider = null;
let signer = null;
let contract = null;
let userAccount = null;
let contractABI = []; // You'll need to add your contract's ABI here

// Contract configuration - Update these with your contract details
const CONTRACT_CONFIG = {
    // Add your contract address here
    address: "", 
    // Add your contract ABI here - this is a basic ERC20-like example
    abi: [
        "function name() view returns (string)",
        "function symbol() view returns (string)",
        "function totalSupply() view returns (uint256)",
        "function balanceOf(address) view returns (uint256)",
        "function transfer(address to, uint256 amount) returns (bool)",
        "function approve(address spender, uint256 amount) returns (bool)",
        "function allowance(address owner, address spender) view returns (uint256)",
        "event Transfer(address indexed from, address indexed to, uint256 amount)",
        "event Approval(address indexed owner, address indexed spender, uint256 amount)"
    ]
};

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Bind event listeners
    document.getElementById('connectWallet').addEventListener('click', connectWallet);
    document.getElementById('connectContract').addEventListener('click', connectContract);
    document.getElementById('readContract').addEventListener('click', readContract);
    document.getElementById('writeContract').addEventListener('click', writeContract);
    document.getElementById('closeNotification').addEventListener('click', closeNotification);
    
    // Set default contract address if provided
    if (CONTRACT_CONFIG.address) {
        document.getElementById('contractAddress').value = CONTRACT_CONFIG.address;
    }
    
    // Check if wallet is already connected
    checkWalletConnection();
}

async function checkWalletConnection() {
    if (typeof window.ethereum !== 'undefined') {
        try {
            const accounts = await window.ethereum.request({ method: 'eth_accounts' });
            if (accounts.length > 0) {
                await initializeProvider();
                showNotification('Wallet already connected', 'success');
            }
        } catch (error) {
            console.error('Error checking wallet connection:', error);
        }
    }
}

async function connectWallet() {
    if (typeof window.ethereum === 'undefined') {
        showNotification('Please install MetaMask or another Web3 wallet', 'error');
        return;
    }

    try {
        showLoading(true);
        
        // Request account access
        await window.ethereum.request({ method: 'eth_requestAccounts' });
        
        await initializeProvider();
        showNotification('Wallet connected successfully!', 'success');
    } catch (error) {
        console.error('Error connecting wallet:', error);
        showNotification('Failed to connect wallet: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

async function initializeProvider() {
    // Initialize provider and signer
    provider = new ethers.BrowserProvider(window.ethereum);
    signer = await provider.getSigner();
    userAccount = await signer.getAddress();
    
    // Update UI
    updateWalletInfo();
    updateNetworkInfo();
    
    // Listen for account changes
    window.ethereum.on('accountsChanged', handleAccountsChanged);
    window.ethereum.on('chainChanged', handleChainChanged);
}

async function updateWalletInfo() {
    if (!userAccount) return;
    
    try {
        const balance = await provider.getBalance(userAccount);
        const balanceInEth = ethers.formatEther(balance);
        
        document.getElementById('accountAddress').textContent = 
            `Account: ${userAccount.substring(0, 6)}...${userAccount.substring(userAccount.length - 4)}`;
        document.getElementById('balance').textContent = 
            `Balance: ${parseFloat(balanceInEth).toFixed(4)} ETH`;
        
        // Show wallet info and hide connect button
        document.getElementById('walletInfo').classList.remove('hidden');
        document.getElementById('connectWallet').style.display = 'none';
    } catch (error) {
        console.error('Error updating wallet info:', error);
    }
}

async function updateNetworkInfo() {
    if (!provider) return;
    
    try {
        const network = await provider.getNetwork();
        const networkNames = {
            1: 'Ethereum Mainnet',
            5: 'Goerli Testnet',
            11155111: 'Sepolia Testnet',
            137: 'Polygon Mainnet',
            80001: 'Polygon Mumbai'
        };
        
        const networkName = networkNames[network.chainId] || `Chain ID: ${network.chainId}`;
        document.getElementById('networkName').textContent = networkName;
    } catch (error) {
        console.error('Error updating network info:', error);
        document.getElementById('networkName').textContent = 'Unknown Network';
    }
}

async function connectContract() {
    const contractAddress = document.getElementById('contractAddress').value.trim();
    
    if (!contractAddress) {
        showNotification('Please enter a contract address', 'error');
        return;
    }
    
    if (!ethers.isAddress(contractAddress)) {
        showNotification('Invalid contract address', 'error');
        return;
    }
    
    if (!signer) {
        showNotification('Please connect your wallet first', 'error');
        return;
    }
    
    try {
        showLoading(true);
        
        // Use provided ABI or default ABI
        const abiToUse = CONTRACT_CONFIG.abi.length > 0 ? CONTRACT_CONFIG.abi : [
            "function name() view returns (string)",
            "function symbol() view returns (string)",
            "function totalSupply() view returns (uint256)"
        ];
        
        contract = new ethers.Contract(contractAddress, abiToUse, signer);
        
        // Test connection by calling a simple read function
        try {
            await contract.name();
            showNotification('Successfully connected to contract!', 'success');
        } catch (readError) {
            // Contract might not have name() function, but connection might still work
            showNotification('Connected to contract (some functions may not be available)', 'success');
        }
        
    } catch (error) {
        console.error('Error connecting to contract:', error);
        showNotification('Failed to connect to contract: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

async function readContract() {
    const functionName = document.getElementById('readFunction').value.trim();
    const params = document.getElementById('readParams').value.trim();
    
    if (!functionName) {
        showNotification('Please enter a function name', 'error');
        return;
    }
    
    if (!contract) {
        showNotification('Please connect to a contract first', 'error');
        return;
    }
    
    try {
        showLoading(true);
        
        // Parse parameters
        const paramArray = params ? params.split(',').map(param => param.trim()) : [];
        
        // Call the contract function
        const result = await contract[functionName](...paramArray);
        
        // Display result
        const resultDiv = document.getElementById('readResult');
        resultDiv.className = 'result success';
        resultDiv.textContent = `Result: ${result.toString()}`;
        
    } catch (error) {
        console.error('Error reading contract:', error);
        const resultDiv = document.getElementById('readResult');
        resultDiv.className = 'result error';
        resultDiv.textContent = `Error: ${error.message}`;
    } finally {
        showLoading(false);
    }
}

async function writeContract() {
    const functionName = document.getElementById('writeFunction').value.trim();
    const params = document.getElementById('writeParams').value.trim();
    const ethValue = document.getElementById('ethValue').value.trim();
    
    if (!functionName) {
        showNotification('Please enter a function name', 'error');
        return;
    }
    
    if (!contract) {
        showNotification('Please connect to a contract first', 'error');
        return;
    }
    
    try {
        showLoading(true);
        
        // Parse parameters
        const paramArray = params ? params.split(',').map(param => param.trim()) : [];
        
        // Prepare transaction options
        const txOptions = {};
        if (ethValue && parseFloat(ethValue) > 0) {
            txOptions.value = ethers.parseEther(ethValue);
        }
        
        // Call the contract function
        const tx = await contract[functionName](...paramArray, txOptions);
        
        // Display transaction hash immediately
        const resultDiv = document.getElementById('writeResult');
        resultDiv.className = 'result info';
        resultDiv.innerHTML = `Transaction sent!<br>Hash: ${tx.hash}<br>Waiting for confirmation...`;
        
        // Add to transaction history
        addTransactionToHistory(functionName, tx.hash, 'pending');
        
        // Wait for confirmation
        const receipt = await tx.wait();
        
        // Update result with confirmation
        resultDiv.className = 'result success';
        resultDiv.innerHTML = `Transaction confirmed!<br>Hash: ${tx.hash}<br>Block: ${receipt.blockNumber}<br>Gas used: ${receipt.gasUsed.toString()}`;
        
        // Update transaction history
        updateTransactionInHistory(tx.hash, 'confirmed');
        
        showNotification('Transaction confirmed!', 'success');
        
    } catch (error) {
        console.error('Error writing to contract:', error);
        const resultDiv = document.getElementById('writeResult');
        resultDiv.className = 'result error';
        resultDiv.textContent = `Error: ${error.message}`;
        
        showNotification('Transaction failed: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

function addTransactionToHistory(functionName, txHash, status) {
    const historyContainer = document.getElementById('transactionHistory');
    
    // Remove "no transactions" message if present
    const noTransactions = historyContainer.querySelector('.no-transactions');
    if (noTransactions) {
        noTransactions.remove();
    }
    
    // Create transaction item
    const txItem = document.createElement('div');
    txItem.className = 'transaction-item';
    txItem.id = `tx-${txHash}`;
    
    txItem.innerHTML = `
        <h4>${functionName}</h4>
        <p>Hash: ${txHash.substring(0, 10)}...${txHash.substring(txHash.length - 8)}</p>
        <p>Status: <span class="status">${status}</span></p>
        <p>Time: ${new Date().toLocaleTimeString()}</p>
    `;
    
    // Add to top of history
    historyContainer.insertBefore(txItem, historyContainer.firstChild);
}

function updateTransactionInHistory(txHash, status) {
    const txItem = document.getElementById(`tx-${txHash}`);
    if (txItem) {
        const statusSpan = txItem.querySelector('.status');
        if (statusSpan) {
            statusSpan.textContent = status;
        }
    }
}

function handleAccountsChanged(accounts) {
    if (accounts.length === 0) {
        // User disconnected wallet
        resetApp();
        showNotification('Wallet disconnected', 'info');
    } else {
        // User switched accounts
        initializeProvider();
        showNotification('Account changed', 'info');
    }
}

function handleChainChanged(chainId) {
    // Reload the page when chain changes
    window.location.reload();
}

function resetApp() {
    provider = null;
    signer = null;
    contract = null;
    userAccount = null;
    
    // Reset UI
    document.getElementById('walletInfo').classList.add('hidden');
    document.getElementById('connectWallet').style.display = 'block';
    document.getElementById('networkName').textContent = 'Not Connected';
    document.getElementById('readResult').textContent = '';
    document.getElementById('writeResult').textContent = '';
}

function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (show) {
        overlay.classList.remove('hidden');
    } else {
        overlay.classList.add('hidden');
    }
}

function showNotification(message, type = 'info') {
    const notification = document.getElementById('notification');
    const messageSpan = document.getElementById('notificationMessage');
    
    messageSpan.textContent = message;
    notification.className = `notification ${type}`;
    notification.classList.remove('hidden');
    
    // Auto hide after 5 seconds
    setTimeout(() => {
        notification.classList.add('hidden');
    }, 5000);
}

function closeNotification() {
    document.getElementById('notification').classList.add('hidden');
}

// Utility function to format addresses
function formatAddress(address) {
    if (!address) return '';
    return `${address.substring(0, 6)}...${address.substring(address.length - 4)}`;
}

// Utility function to format large numbers
function formatNumber(num) {
    if (!num) return '0';
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
</script>
