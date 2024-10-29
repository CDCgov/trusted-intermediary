# Scripts

## Instructions

1. Copy `.env.template` to `.env`
    ```
    cp .env.template .env
    ```
2. Edit `.env` and make sure to update at least `CDCTI_HOME` (local path to the `trusted-intermediary` codebase) and `RS_HOME` (local path to the `prime-reportstream` codebase) are set. **Note**: if you don't set `CDCTI_HOME`, none of these scripts will work. Also, please use `$HOME` or the full path to your home directory instead of `~`
3. Load the environment variables in `.env` by running
   ```
   source .env
   ```
   **Note**: you may also want to add it to your shell's startup file so you don't need to run it for every terminal session.
4. Run you script
