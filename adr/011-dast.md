# 11. OWASP ZAP Dynamic Application Security Testing (DAST)

Date: 2023-02-06

## Decision

- **OWASP** - [Open Web Application Security Project](https://owasp.org/about)
- **ZAP** - Zed Attack Proxy 

We will use [OWASP ZAP](https://www.zaproxy.org/) for DAST scanning of our application.

## Status

Accepted.

## Context

ZAP is a free, open-source penetration testing tool maintained under the umbrella of the Open Web Application Security Project (OWASP). ZAP is designed specifically for testing web applications and is both flexible and extensible.

[Reference](https://www.zaproxy.org/getting-started/)

## Impact

### Positive
  
- **Integration Capabilities:** ZAP can be integrated into CI/CD pipelines, enabling automated security testing as part of the software development lifecycle.

  
- **User-Friendly Interface:** ZAP has an easy-to-use interface that simplifies the process of running scans and analyzing results, making it accessible for teams with varying levels of expertise.

  
- **Comprehensive Reporting:** ZAP can generate detailed reports that help identify vulnerabilities and guide remediation efforts, useful for compliance and audits.


- **Community Support:** The active community surrounding ZAP provides a wealth of plugins, resources, and support, facilitating easier troubleshooting and best practices sharing.

### Negative

- **Learning Curve:** Teams new to penetration testing tools may face a learning curve to fully leverage ZAP's capabilities.


- **Resource Intensive:** Running ZAP scans, especially on larger applications, can consume significant system resources and impact performance during testing.


- **False Positives:** Like many DAST tools, ZAP may produce false positives, requiring additional time for manual verification of identified vulnerabilities.


- **Limited Advanced Features:** While ZAP is powerful, it may lack some advanced features found in commercial DAST tools, such as specialized vulnerability assessments.


### Risks

- **Configuration Errors:** Incorrect configuration of ZAP may lead to incomplete scans or misinterpretation of results, potentially leaving vulnerabilities undetected.

- **Production Systems Impact:** Running DAST scans on production environments can introduce performance issues or impact user experience if not managed carefully.

- **Reliance on Community Support:** Relying on community resources for troubleshooting can lead to slower issue resolution compared to commercial tools with dedicated support.


### Related Issues

- #77
