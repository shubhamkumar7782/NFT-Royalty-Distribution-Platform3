

// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Royalty.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

/**
 * @title NFTRoyaltyDistribution
 * @dev A contract for minting NFTs with royalty distribution to multiple creators
 */
contract NFTRoyaltyDistribution is ERC721Royalty, Ownable {
    using Counters for Counters.Counter;
    Counters.Counter private _tokenIdCounter;

    // Mapping from token ID to token URI
    mapping(uint256 => string) private _tokenURIs;

    // Mapping from token ID to array of royalty recipients
    mapping(uint256 => address[]) private _royaltyRecipients;
    
    // Mapping from token ID to array of royalty shares (in basis points, e.g., 500 = 5%)
    mapping(uint256 => uint256[]) private _royaltyShares;

    // Events
    event NFTMinted(uint256 indexed tokenId, address indexed creator, string tokenURI);
    event RoyaltyDistributed(uint256 indexed tokenId, address indexed recipient, uint256 amount);

    constructor() ERC721("NFT Royalty Distribution", "NFTRD") Ownable(msg.sender) {}

    /**
     * @dev Mints a new NFT with specified royalty information
     * @param tokenURI The URI for the NFT metadata
     * @param recipients Array of addresses to receive royalties
     * @param shares Array of shares for each recipient (in basis points, e.g., 500 = 5%)
     * @return tokenId The ID of the newly minted NFT
     */
    function mintNFT(
        string memory tokenURI,
        address[] memory recipients,
        uint256[] memory shares
    ) public returns (uint256) {
        require(recipients.length == shares.length, "Recipients and shares arrays must have the same length");
        require(recipients.length > 0, "At least one royalty recipient is required");
        
        uint256 totalShares = 0;
        for (uint256 i = 0; i < shares.length; i++) {
            totalShares += shares[i];
        }
        require(totalShares <= 10000, "Total shares cannot exceed 100%");

        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        _safeMint(msg.sender, tokenId);
        _setTokenURI(tokenId, tokenURI);
        
        // Store royalty recipients and shares
        _royaltyRecipients[tokenId] = recipients;
        _royaltyShares[tokenId] = shares;
        
        // Set default royalty info in ERC721Royalty for marketplaces to use
        _setTokenRoyalty(tokenId, address(this), uint96(totalShares));
        
        emit NFTMinted(tokenId, msg.sender, tokenURI);
        
        return tokenId;
    }

    /**
     * @dev Distributes royalties to all recipients for a specific NFT
     * @param tokenId The ID of the NFT
     */
    function distributeRoyalty(uint256 tokenId) external payable {
        require(_ownerOf(tokenId) != address(0), "NFT does not exist");
        require(msg.value > 0, "No royalty payment provided");
        
        address[] memory recipients = _royaltyRecipients[tokenId];
        uint256[] memory shares = _royaltyShares[tokenId];
        
        uint256 totalShares = 0;
        for (uint256 i = 0; i < shares.length; i++) {
            totalShares += shares[i];
        }
        
        uint256 remainder = msg.value;
        
        // Distribute royalties based on shares
        for (uint256 i = 0; i < recipients.length; i++) {
            if (i == recipients.length - 1) {
                // Last recipient gets remainder to avoid rounding issues
                payable(recipients[i]).transfer(remainder);
                emit RoyaltyDistributed(tokenId, recipients[i], remainder);
            } else {
                uint256 amount = (msg.value * shares[i]) / totalShares;
                payable(recipients[i]).transfer(amount);
                remainder -= amount;
                emit RoyaltyDistributed(tokenId, recipients[i], amount);
            }
        }
    }

    /**
     * @dev Returns the royalty information for a token
     * @param tokenId The ID of the NFT
     * @return recipients Array of royalty recipients
     * @return shares Array of shares for each recipient
     */
    function getRoyaltyInfo(uint256 tokenId) external view returns (address[] memory recipients, uint256[] memory shares) {
        require(_ownerOf(tokenId) != address(0), "NFT does not exist");
        return (_royaltyRecipients[tokenId], _royaltyShares[tokenId]);
    }

    /**
     * @dev Sets the token URI for a specific token ID
     * @param tokenId The ID of the token to set its URI
     * @param tokenURI The URI to assign
     */
    function _setTokenURI(uint256 tokenId, string memory tokenURI) internal {
        _tokenURIs[tokenId] = tokenURI;
    }

    /**
     * @dev Returns the token URI for a given token ID
     * @param tokenId The ID of the token to query
     * @return The token URI for the given token ID
     */
    function tokenURI(uint256 tokenId) public view override returns (string memory) {
        if (_ownerOf(tokenId) == address(0)) {
            revert("ERC721: invalid token ID");
        }
        return _tokenURIs[tokenId];
    }
}


