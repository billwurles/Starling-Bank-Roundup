# Starling-Bank-Roundup
Program that communicates with the Starling Bank API, gets account details, calculates the value of rounding up outgoing transactions and transferring that value to a new Savings Goal

This is written using Spring Boot, with Lombok for brevity. To execute the roundup, run the program and navigate to:

      http://localhost:8080/api/roundup?from=2025-02-25
      
in the browser.

      
Additionally Account information and Savings Goal information can be retrieved at respectively:

/api/account

/api/account/{account-uid}/savings-goal/{saving-goal-uid}


Written by Will Burles
