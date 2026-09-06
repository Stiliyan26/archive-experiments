#start from the "santa" directory
cd ./frontend/

# installs fnm (Fast Node Manager)
winget install Schniz.fnm

# configure fnm environment
fnm env --use-on-cd | Out-String | Invoke-Expression

# download and install Node.js
fnm use --install-if-missing 14

# verifies the right Node.js version is in the environment
#node -v # should print `v14.21.3`

# verifies the right npm version is in the environment
#npm -v # should print `6.14.18`

npm run dev