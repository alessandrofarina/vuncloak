
## Vuncloak
Vuncloak is an open-source Java software that allows you to scan public GitHub Maven-based repositories
for vulnerable third-party libraries.

### How It Works?
Vuncloak relies on [JGit](https://git-scm.com/book/it/v2/Appendice-B%3A-Embedding-Git-in-your-Applications-JGit) to interact with the repository.
It parses every available version of the POM descriptor searching for dependencies to then 
makes a request to the [Sonatype OSS Index API](https://ossindex.sonatype.org/rest) to obtain a JSON list of vulnerabilities that affects them.
Once completed the analysis, Vuncloak appends the resume of the history of the repository to a _report.cvs_ file you can read afterward.

### Requirements
You must be signed to the Sonatype OSS Index API. You're going to need _email_ and _token_ to authorize the API from the application.

### Installation
You can download the package from the [release](https://github.com/alessandrofarina/vuncloak/releases/tag/v1.0).
Copy your Sonatype OSS Index API _email_ and _token_ to _credentials.txt_ and the list of repository you mean to scan to _repositories.txt_.
Then run the JAR file, Vuncloak will take care of the rest showing you the progress via real-time log. Once it's finished, you can also read the _report.csv_ produced.

### Histories: A Dataset made w/ Vuncloak
We tested Vuncloak on a sample of 30 repositories available on GitHub. You can take a look at the resulting dataset, called _Histories_, [here](https://github.com/alessandrofarina/vuncloak/blob/master/histories.csv).
