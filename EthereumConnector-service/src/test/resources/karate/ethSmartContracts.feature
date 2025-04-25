# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check Smart Contracts Rest Api Response

  Scenario: Ethereum Smart Contracts CRUD Operations

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/smart-contracts'
    # ---- Add entity fields here -----
    And request 
    """ { "contractClass": "exampleField","name":"contract","address":"address","transactionReceipt":"transaction","blockchainId": "#(blockchainId)"}; """
    # ---------------------------------
    When method POST
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":1,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "address":"address",
        "contractClass": 'exampleField',
        "name":"contract",
        "transactionReceipt":"transaction",
        "blockchainId": #number
       }
    """
    * def entityId = response.id
    
    # --------------- UPDATE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/smart-contracts'
    # ---- Add entity fields here -----
    And request 
    """ { 
          "id":"#(entityId)",
          "entityVersion":1,
          "address":"address",
          "contractClass": 'exampleFieldUpdated',
          "name":"contract",
          "transactionReceipt":"transaction",
          "blockchainId": "#(blockchainId)"
    } 
    """
    # ---------------------------------
    When method PUT
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "address":"address",
        "contractClass": 'exampleFieldUpdated',
        "name":"contract",
        "transactionReceipt":"transaction",
        "blockchainId": #number
       }
    """
  
  # --------------- FIND -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/smart-contracts/'+entityId
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "address":"address",
        "contractClass": 'exampleFieldUpdated',
        "name":"contract",
        "transactionReceipt":"transaction",
        "blockchainId": #number
       }
    """
    
  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/smart-contracts'
    When method GET
    Then status 200
    And match response.results contains
    """
      {
        "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "address":"address",
        "contractClass": 'exampleFieldUpdated',
        "name":"contract",
        "transactionReceipt":"transaction",
        "blockchainId": #number
      }
    """
  
  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/smart-contracts/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
