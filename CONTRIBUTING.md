# Welcome!

Thank you for contributing to CDC's Open Source projects! If you have any
questions or doubts, don't be afraid to send them our way. We appreciate all
contributions, and we are looking forward to fostering an open, transparent, and
collaborative environment.

Before contributing, we encourage you to also read or [LICENSE](LICENSE),
[README](README.md), [code of conduct](code-of-conduct.md), and [ADRs](/adr).
If you have any inquiries or questions not answered by the content of this repository, feel free to
[contact us](mailto:surveillanceplatform@cdc.gov).

## Public Domain

This project is in the public domain within the United States, and copyright and
related rights in the work worldwide are waived through the
[CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
All contributions to this project will be released under the CC0 dedication. By
submitting a pull request you are agreeing to comply with this waiver of
copyright interest.

## Requesting Changes

Our pull request/merging process is designed to give the CDC Surveillance Team
and other in our space an opportunity to consider and discuss any suggested
changes. This policy affects all CDC spaces, both on-line and off, and all users
are expected to abide by it.

### Open an issue in the repository

If you don't have specific language to submit but would like to suggest a change
or have something addressed, you can open an issue in this repository. Team
members will respond to the issue as soon as possible.

### Submit a pull request

If you would like to contribute, please submit a pull request. In order for us
to merge a pull request, it must:

- Be at least seven days old. Pull requests may be held longer if necessary
  to give people the opportunity to assess it.
- Receive a +1 from a majority of team members associated with the request.
  If there is significant dissent between the team, a meeting will be held to
  discuss a plan of action for the pull request.

## Commit Signing

[Signing your commits](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits) ensures that you truly authored them.  Technically, there is nothing built into `git` that stops me from changing my git author information to be a forgery of yours and making commits that look like they came from you.  Commit signing relies on PKI which is incredibly hard to break.

### Prerequsites

You need to install GPG.  If you are on a Mac, you can do this easily with [Brew](https://brew.sh).

```shell
brew install gnupg
```

### Create a Signing Key

Follow the steps outlined on [GitHub's documentation for generating a GPG key](https://docs.github.com/en/authentication/managing-commit-signature-verification/generating-a-new-gpg-key), but consider the following.

- When it asks for what kind of key you want, pick `RSA (sign only)` (which is commonly option 4).
- When it asks for what key size you want, enter in `4096`.
- Remember the password you set!

### Add the Key to GitHub

Follow the steps outlined on [GitHub's documentation for adding your GPG key](https://docs.github.com/en/authentication/managing-commit-signature-verification/adding-a-gpg-key-to-your-github-account).

### Set-up Git to Sign Commits

Follow just the GPG key steps outlined on [GitHub's documentation for configuring git with your signing key](https://docs.github.com/en/authentication/managing-commit-signature-verification/telling-git-about-your-signing-key), but consider the following.

- When running the `git config` commands, you may not want to use `--global` unless you want the settings to apply to all your cloned git repositories, even ones that aren't from GitHub.
- You will only need to install and configure a GUI PIN entry program (for example, `pinentry-mac`) if you don't do your commits in a terminal.  For example, a non-terminal, GUI interface in your IDE or the GitHub application.

### (Optional) Move the Key to a YubiKey

Following these steps will result in your YubiKey holding your signing key.  This will require your YubiKey to be plugged-in whenever you make a commit.  You will no longer need to remember the password for the key but instead need to enter the YubiKey PIN from time to time.

Follow the steps outlined on [asdf](https://github.com/drduh/YubiKey-Guide#configure-smartcard), but consider the following.

- There's no need to set the information on the YubiKey like the name, lang, or login.
- If you've followed this process, there is just the ultimate key to move over, not any sub keys.  You'll only move over the ultimate key to the signature key (not encryption, not authentication) part of the YubiKey.
