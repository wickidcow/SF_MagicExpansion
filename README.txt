MagicExpansion guide-name fix
================================

Purpose
-------
Changes the main Slimefun guide category title from the obfuscated "2.0"
display to:

    Magic

The LIGHT icon, NamespacedKey, item IDs, category order, recipes, and saved
data are not changed.

Windows
-------
1. Extract all files from this ZIP into the root of your SF_MagicExpansion
   source repository.
2. Double-click:

       apply-magic-guide-name-fix.bat

3. Commit the changed Java file and run your normal GitHub Actions build.

Linux/macOS
-----------
From the repository root:

    python3 apply-magic-guide-name-fix.py

Changed source file
-------------------
src/main/java/io/Yomicer/magicExpansion/MagicExpansionItemSetup.java

The fixer creates this backup before changing the source:

src/main/java/io/Yomicer/magicExpansion/MagicExpansionItemSetup.java.bak

A one-line unified diff is also included as:
Magic-guide-name-fix.patch
