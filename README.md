#Android SecHeaders

##Table of Contents
1.[Headers to Examine](https://github.com/Rambou/Andro-SecHeaders#Headers to Examine)

2.[References](https://github.com/Rambou/Andro-SecHeaders#References)

3.[License](https://github.com/Rambou/Andro-SecHeaders#License)

###Info
An app for android 4.1+ that takes as an input some websites and then gathers, filter, visualize and generate statistics of their Security Headers. It's started as a mini-project for the course of "Mobile and Wireless Networks Security" at the Department of Information & Communication Systems Engineering, University of Aegean. So don't expect anything fancy lads. :bowtie:

###Headers to Examine
The app will filter all headers to find the ones for security. Those will be filterd are 10 and mentioned above. :wink:
+ __Access Control Allow Origin__ - When Site A tries to fetch content from Site B, Site B can send an Access-Control-Allow-Origin response header to tell the browser that the content of this page is accessible to certain origins.
+ __Content Security Policy (CSP)__ - Helps detect/prevent XSS, mixed-content, and other classes of attack. CSP 1.1 Specification.
+ __Cross Domain Meta Policy__ - Tells Flash and PDF files which Cross Domain Policy files found on your site can be obeyed; yes, it's a policy about other policies!
+ __Server Information__ - Who has a need to know what type of server you're running?
+ __UTF-8 Character Encoding__ - Minimizing the likelihood that malicious character conversion could happen.
+ __X-Frame-Options (XFO)__ - Prevents your content from being framed and potentially clickjacked. X-Frame-Options draft.
+ __X-Powered-By__ - Who has a need to know what software version you're running?
+ __X-XSS-Protection__ - Cross site scripting heuristic filter for IE/Chrome.
+ __X-Content-Type-Options__ - Prevent content type sniffing "__NoSniff__".
+ __X-Download-Options__ - Prevent file downloads opening.
+ __HTTP Strict Transport Security (HSTS)__ - Ensures the browser never visits the http version of a website. Protects SSLStrip/Firesheep attacks. HSTS Specification
+ __Secure Cookies__ - Ensure that the server knows the client. Checking the use of __Set-Cookie2__ along to the insecure __Set-Cookie__.
+ __X-Pingback__ - Header for blogs, a url that allows other sites that link to that site/site's article to tell the site that it did link to that site. Before adding it, used for DDOS attacks.

##References
* Course: http://www.icsd.aegean.gr/icsd_en/proptyxiaka/istoselida_mathimatos.php?lesson_id=321-10752
* Department: http://www.icsd.aegean.gr/icsd_en/
* Secure Headers: https://securityheaders.com/
* OWASP: https://www.owasp.org/index.php/List_of_useful_HTTP_headers

##License
Android SecHeaders, are released under the terms of the [MIT license](http://en.wikipedia.org/wiki/MIT_License).
The MIT License is simple and easy to understand and it places almost no restrictions on what you can do with a "Android SecHeaders" project. You are free to use any "Android SecHeaders" project in any other project (even commercial projects) as long as the copyright header is left intact.
