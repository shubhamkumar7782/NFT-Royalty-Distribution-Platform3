const { ethers } = require("hardhat");

async function main() {
  console.log("Deploying NFTRoyaltyDistribution contract...");

  // Get the contract factory
  const NFTRoyaltyDistribution = await ethers.getContractFactory("NFTRoyaltyDistribution");
  
  // Deploy the contract
  const nftRoyaltyDistribution = await NFTRoyaltyDistribution.deploy();

  // Wait for deployment to finish
  await nftRoyaltyDistribution.waitForDeployment();
  
  // Get the contract address
  const contractAddress = await nftRoyaltyDistribution.getAddress();
  
  console.log(`NFTRoyaltyDistribution deployed to: ${contractAddress}`);
  console.log(`Verify contract with: npx hardhat verify --network coretestnet2 ${contractAddress}`);
}

// Execute the deployment
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
