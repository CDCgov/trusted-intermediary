# Hurl Scripts

## Requirements

- [hurl](https://hurl.dev/)

## ReportStream

### Sending Orders

1. Edit `waters.hurl` to update the line with `file,Orders/<your_file_name_here>.hl7;` with the order you want to send.
   1. If running on staging, edit `hrl` script to set the `secret` variable to point to the staging private key, which should be downloaded from Keybase.
2. Run the script:
    ```
    ./hrl <env> waters.hurl
    ```
    where `<env>` is either `local` or `staging`


### Checking history

```
./hrl <env> history.hurl --variable submissionid=<submission_id>
```
