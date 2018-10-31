# inrideads

## Version History

### X.XX (Working Version)

Changes:

- List changes here


### 2.58
April 27, 2018
Changes:
    - Direct caching improved - app parses adschedule and config and initiates caching for missing
    videos without a need to play any screensaver or tab.


### 2.55
Changes:
	- Adapted health monitoring service from monitoring app to inrideads. It will be activated only if there is no Edison monitoring app installed in the system.
	- Bug fixes

### 2.53
Changes:
	- Implemented new diag screen with simple download progress output.
	- Added register by group code (install code) to first run screen and radmin
	- Bug fixes


### 2.50
April 16, 2018
Changes:
	- Application will retry to download aol cached video in case if it failed to download.
	- Bug fixes

### 2.48
April 7, 2018
Changes: 
	- Implemented Google geolocation service. It will be active on IEI screens only. Locations with accuracy > 2000 meters will be ignored.
		
### 2.39
2018.03.30
Changes:
	- First version that implements AOL cache
	- Added markdown files README.md, DESIGN.md, CHANGELOG.md
