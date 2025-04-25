# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check Blockchains Rest Api Response

  Scenario: Ethereu Blockchains CRUD Operations

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/blockchains'
    # ---- Add entity fields here -----
    And request 
    """ { "protocol": "http","host":"host","port":"123123"}; """
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
        "protocol":"http",
        "host": 'host',
        "port":"123123"
       }
    """
    * def entityId = response.id
    
    # --------------- UPDATE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/blockchains'
    # ---- Add entity fields here -----
    And request 
    """ { 
          "id":"#(entityId)",
          "entityVersion":1,
          "protocol":"http",
          "host": 'host',
          "port":"123123"
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
        "protocol":"http",
        "host": 'host',
        "port":"123123"
       }
    """
  
  # --------------- FIND -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/blockchains/'+entityId
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
        "protocol":"http",
        "host": 'host',
        "port":"123123"
       }
    """
    
  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/blockchains'
    When method GET
    Then status 200
    And match response.results contains
    """
      {
        "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "protocol":"http",
        "host": 'host',
        "port":"123123"
      }
    """
  
  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/ethereum/blockchains/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
