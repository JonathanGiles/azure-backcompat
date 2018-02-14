# azure-backcompat
Project that generates a report detailing all backwards incompatible changes between specified versions of an SDK. 
Results of each run are [published online](https://jonathangiles.github.io/azure-backcompat) for easy review.

To specify what releases of SDKs should be compared, the releases.json file should be edited. It is this file that is loaded at runtime to determine the comparisons that will be run and output into the report.
