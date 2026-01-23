# CSEP Final Project | Team 36

Prerequisites
- Java JDK 25
- Maven (or use the included mvnw wrapper)

How to run our project

1. Start the server:
   Open a terminal in the project root and execute:
   mvn -pl server -am spring-boot:run
2. Start the client:
   Once the server is running, open a new terminal window in the project root and execute:
   mvn clean compile
   mvn -pl client -am javafx:run

Please note that the server needs to be running, before you can start the client!
Get the project running from the command line first to ensure you have the required tools on your system.

If you prefer using your IDE to run the client, you must set up OpenJFX. To do so, follow these steps:
1. Download and unzip the OpenJFX SDK, matching your JDK version
2. Create a run configuration for the main class.
3. Add the following commands to the VM Options field:
   --module-path="/path/to/javafx-sdk/lib" --add-modules=javafx.controls,javafx.fxml,javafx.web

Adjust the module path to your local download location and ensure the path points
to the `lib` directory.

*Tip:* If you are using Windows, use backslashes (`\`) and start the path with a drive letter like `C:`.
*Tip:* If you receive errors like `Module javafx.web not found` or a segmentation fault, verify that
your path points specifically to the lib folder containing the .jar files.

Implemented Extension Ideas

1. Shopping List
   We added the option to create a new food item (ingredient) through the shopping list. See below for instructions on how to use our extension.

2. Live Language Switch
   We implemented 6 languages (exceeding the required 3):
   - English
   - Dutch
   - Romanian
   - Bulgarian
   - German
   - Slovak

User Guide

Refresh Button
While the application uses WebSockets for automatic updates, a Refresh button is still used so that the user can retrieve changes for recipes they are not currently viewing.

How to Edit/Add/Delete Ingredients in a recipe
- To modify an existing ingredient:
1. Select the ingredient from the list.
2. Make changes using the input fields.
3. Click the 'Update' button to confirm the change in the list.
- To add a new ingredient:
1. Fill in the fields.
2. Click the 'Add' button.
- To delete an ingredient:
1. Select the ingredient from the list.
2. Click the ‘Remove’ button.
Once your ingredient list is ready, click the 'Apply' button to save all the changes to the server and return to the main ui.

How to Create a New Food Item
1. Via the Create Recipe/Edit Ingredients screen
Click the ingredient name dropdown menu.
Scroll to the very bottom of the list.
Select ‘+ Create New Food Item…’
Result: You will be redirected to the "Add Food Item" screen. After creating the new item and clicking ‘Confirm’, you will be returned to your recipe draft exactly where you left off.
2. Via the Shopping List
Click the add button (‘+’) on the bottom left of the shopping list view.
Type the name of the new item into the input field (first box) instead of selecting an existing one from the dropdown menu.
Fill in the other 2 fields (quantity and unit) and click ‘OK’.
Result: A prompt will ask if you want to create this unknown item. Click ‘OK’ to be redirected to the "Add Food Item" screen. After clicking ‘Confirm’, you are automatically returned to your shopping list, allowing you to continue where you left off.

Notes for using the searching feature:
A comma (“,”) is used as an AND operator. So for complex searches with multiple keywords, separate the keywords by a comma and a space; an example is: “Banana, Honey”

Notes for editing/deleting a food item:
After editing or removing a food item, make sure to re-select the recipe in order to see the updated changes.

