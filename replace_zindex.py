import os
import glob

# Files to process
files = [
    "/Users/alexisjavaux/Documents/Ceberus/cerberus-core/source/src/main/webapp/include/transversal/TestDataLib.html",
    "/Users/alexisjavaux/Documents/Ceberus/cerberus-core/source/src/main/webapp/include/transversal/ApplicationObject.html",
    "/Users/alexisjavaux/Documents/Ceberus/cerberus-core/source/src/main/webapp/include/transversal/TestCaseListMassActionUpdate.html",
    "/Users/alexisjavaux/Documents/Ceberus/cerberus-core/source/src/main/webapp/include/transversal/AppService.html",
    "/Users/alexisjavaux/Documents/Ceberus/cerberus-core/source/src/main/webapp/include/transversal/TestCase.html"
]

for filepath in files:
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # We replace the inline z-index with the new class
        # Look for the crbDropdown panel container template
        # Usually it's: class="... max-h-64 overflow-hidden" style="z-index:9999;"
        content = content.replace('style="z-index:9999;"', '')
        content = content.replace('style="z-index: 9999;"', '')
        
        # Then make sure we add the class crb-dropdown-panel to the <div> that is teleported.
        # It usually has 'fixed rounded-md border shadow-lg bg-white...'
        content = content.replace('class="fixed rounded-md', 'class="crb-dropdown-panel fixed rounded-md')
        
        # Write back
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Processed {filepath}")

