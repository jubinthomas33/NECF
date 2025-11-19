# Jenkins Setup Guide for NECF Project

## Prerequisites

1. **Jenkins Server** (version 2.400+ recommended)
2. **JDK 11** (Java 11) installed on Jenkins server
3. **Maven 3.8+** installed on Jenkins server
4. **Required Jenkins Plugins:**
   - Pipeline Plugin
   - Maven Integration Plugin
   - HTML Publisher Plugin (for viewing reports)
   - TestNG Results Plugin (for test results)
   - Email Extension Plugin (optional, for email notifications)
   - Cucumber Reports Plugin (optional, for Cucumber reports)

## Jenkins Configuration Steps

### 1. Configure JDK and Maven in Jenkins

1. Go to **Jenkins Dashboard** → **Manage Jenkins** → **Global Tool Configuration**
2. Under **JDK installations**:
   - Click **Add JDK**
   - Name: `JDK-11` (or any name you prefer, update Jenkinsfile accordingly)
   - JAVA_HOME: Path to your JDK 11 installation (e.g., `/usr/lib/jvm/java-11-openjdk` or `C:\Program Files\Java\jdk-11.0.27`)
   - Check **Install automatically** if you want Jenkins to download it
3. Under **Maven installations**:
   - Click **Add Maven**
   - Name: `Maven-3.8` (or any name you prefer, update Jenkinsfile accordingly)
   - Version: Select Maven 3.8.6 or higher
   - Check **Install automatically** if you want Jenkins to download it

### 2. Create Jenkins Pipeline Job

1. Go to **Jenkins Dashboard** → **New Item**
2. Enter item name: `NECF-Test-Automation` (or your preferred name)
3. Select **Pipeline** and click **OK**
4. In the pipeline configuration:
   - **Definition**: Select **Pipeline script from SCM**
   - **SCM**: Select your version control (Git, SVN, etc.)
   - **Repository URL**: Enter your repository URL
   - **Credentials**: Add credentials if repository is private
   - **Branch Specifier**: `*/main` or `*/master` (adjust as needed)
   - **Script Path**: `NECF/Jenkinsfile` (path to Jenkinsfile in your repo)

### 3. Configure Environment Variables (Optional)

If you need to customize browser or other settings:

1. Go to **Manage Jenkins** → **Configure System**
2. Under **Global properties** → **Environment variables**:
   - Add `BROWSER=chrome` (or firefox, edge)
   - Add `HEADLESS=true` (for headless execution)

### 4. Install Required Browser Drivers (For Headless Execution)

For headless Chrome execution, ensure Chrome/Chromium is installed on Jenkins server:

**Linux:**
```bash
# Install Chrome
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo dpkg -i google-chrome-stable_current_amd64.deb

# Install ChromeDriver (or use WebDriverManager which handles this automatically)
```

**Windows:**
- Download and install Chrome from Google
- WebDriverManager will handle ChromeDriver automatically

### 5. Configure Email Notifications (Optional)

1. Go to **Manage Jenkins** → **Configure System**
2. Under **Extended E-mail Notification**:
   - Configure SMTP server settings
   - Set default recipient email
3. Uncomment email sections in Jenkinsfile if you want email notifications

## Running the Pipeline

1. Click on your pipeline job
2. Click **Build Now** to trigger a build
3. Monitor the build progress in the console output
4. View test reports after build completion:
   - Extent Reports: Available in build artifacts and HTML Publisher
   - TestNG Reports: Available in Test Results section
   - Screenshots: Available in build artifacts

## Troubleshooting

### Issue: JDK or Maven not found
- **Solution**: Update tool names in Jenkinsfile to match your Jenkins tool configuration names

### Issue: Tests fail with browser/driver errors
- **Solution**: Ensure browser is installed on Jenkins server, or configure headless mode

### Issue: Reports not generated
- **Solution**: Check if tests are actually running. Verify TestRunner class path and feature file location

### Issue: Email not sending
- **Solution**: Configure SMTP settings in Jenkins and uncomment email sections in Jenkinsfile

## Customization

### Change Browser
Update the `BROWSER` environment variable in Jenkinsfile:
```groovy
BROWSER = 'firefox'  // or 'chrome', 'edge'
```

### Disable Headless Mode
Update the `HEADLESS` environment variable:
```groovy
HEADLESS = 'false'
```

### Add More Test Parameters
Modify the test execution stage to include additional Maven parameters:
```groovy
sh '''
    mvn test \
        -Dtest=com.testRunner.TestRunner \
        -Dbrowser=${BROWSER} \
        -Dheadless=${HEADLESS} \
        -Dtags=@smoke  # Add Cucumber tags if needed
'''
```

## Build Artifacts

After each build, the following artifacts are archived:
- `target/ExtentReports/SparkReport.html` - Main Extent HTML report
- `target/ExtentReports/ExtentReport.pdf` - PDF report
- `target/ExtentReports/ExtentJson.json` - JSON report
- `target/htmlReports/htmlReport.html` - Cucumber HTML report
- `target/jsonReports/jsonReport.json` - Cucumber JSON report
- `test-output/**/*` - TestNG reports
- `target/ExtentReports/screenshots/**/*` - Test screenshots

## Notes

- The pipeline is configured for Java 11 (JDK 11) as per your project requirements
- Maven 3.8+ is recommended for compatibility
- WebDriverManager will automatically download browser drivers, so manual driver installation is not required
- Reports are archived for 10 builds (configurable in Jenkinsfile)

