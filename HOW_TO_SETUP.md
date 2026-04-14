# How to Setup (Google Sheets Sync)

This guide will explain how to set up automatic transaction data synchronization (Webhook) from the PerTracker app to your Google Sheets using Google Apps Script.

### Steps:

1. **Open Google Sheets** in your browser (sheets.google.com).
2. **Create a new Spreadsheet** and name the sheet tab **`Transaction`**.
3. In the top menu, click **Extensions**, then select **Apps Script**.
4. **Copy all the code below** and paste it into the Apps Script editor:

   ```javascript
   function doPost(e) {
     // Set your secret API Key here
     var SECRET_API_KEY = "API_KEY_SECRET"; 

     try {
       var payload = JSON.parse(e.postData.contents);
       
       // 1. API Key Validation
       if (payload.api_key !== SECRET_API_KEY) {
         return ContentService.createTextOutput(JSON.stringify({
           "status": "error", 
           "message": "Unauthorized: Invalid API Key!"
         })).setMimeType(ContentService.MimeType.JSON);
       }
       
       var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Transaction");
       
       if (!sheet) {
         throw new Error("Target sheet 'Transaction' not found. Please verify the sheet name.");
       }

       var transactions = [];

       // 2. Data Format Detection
       // Check if payload is Bulk (contains 'transactions' array)
       if (payload.transactions && Array.isArray(payload.transactions)) {
         transactions = payload.transactions;
       } 
       // Check if payload is Single Data (contains 'id' and 'category')
       else if (payload.id && payload.category) {
         transactions.push({
           id: payload.id,
           category: payload.category,
           remarks: payload.remarks,
           nominal: payload.nominal
         });
       } 
       // If format is completely unknown
       else {
         throw new Error("Unrecognized data format. Please provide a valid single or bulk payload.");
       }

       // 3. Prepare 2D Array for batch insert
       var rowsToInsert = [];
       var timestamp = new Date(); // Use identical timestamp for the entire batch
       
       for (var i = 0; i < transactions.length; i++) {
         var trx = transactions[i];
         rowsToInsert.push([
           trx.id,
           trx.category,
           trx.remarks,
           trx.nominal,
           timestamp 
         ]);
       }
       
       // 4. Execute Batch Insert
       var startRow = sheet.getLastRow() + 1;
       var numRows = rowsToInsert.length;
       var numCols = rowsToInsert[0].length; 
       
       sheet.getRange(startRow, 1, numRows, numCols).setValues(rowsToInsert);
       
       return ContentService.createTextOutput(JSON.stringify({
         "status": "success", 
         "message": numRows + " transaction(s) successfully synced to Sheets!"
       })).setMimeType(ContentService.MimeType.JSON);
       
     } catch (error) {
       return ContentService.createTextOutput(JSON.stringify({
         "status": "error", 
         "message": error.toString()
       })).setMimeType(ContentService.MimeType.JSON);
     }
   }
   ```

5. **Change `SECRET_API_KEY`** at the top of the code to a secret token of your choice (e.g., `SecretKey123`).
6. Click the blue **Deploy** button at the top right > **New deployment**.
   - Select type: **Web app**.
   - "Who has access": Change this to **"Anyone"** (This is crucial, set to everyone!).
   - Click **Deploy**, and authorize access with your Google account if prompted.
   - **Copy the Endpoint link (Web app URL)** that starts with `https://script.google.com/macros/s/.../exec`.
7. **Open the PerTracker app** on your Android device.
8. Tap the **Configuration / Settings button** on the Dashboard menu. 
   - **Copy & Paste the Endpoint Link** from your Apps Script into the **Target URL** field.
   - **Paste the API Key** you created in step 5 into the **API Key** field.
9. **(Optional)** Toggle the **Enable Auto Sync Button** if you want the app to automatically fire the data to Google Sheets immediately after a transaction is saved.
