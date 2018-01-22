![labtablet UI](https://raw.githubusercontent.com/feup-infolab/labtablet/master/labtablet.png "LabTablet UI")

# README #

This README summarizes the LabTablet purpose and features. This repository holds the code and features developed after the first iteration (from February 2014 until July 2014).

### How do I get set up? ###

* Summary of set up

Setting up LabTablet is a pretty straightforward task. The project was developed in the android studio IDE, and thus it can easily be opened by this application. After the first launch, graddle will take some time to find and solve any dependencies and configure the workbench. LabTablet requires Android KitKat to run. Although its compatibility can later be expanded to older Android Versions, for now some of the API calls will not work on API 18 and below.

* Dependencies

Some libraries are needed to handle specific features of the LabTablet interface:

1. For networking, I used the Apache http components that can be found either inside the project or directly on their website;

2. To receive valuable debug information, I used the project [ACRA](https://code.google.com/p/acra/) that submits crash reports to a webserver specifically designed to this purpose. This is only transmitted when the application crashes. Other less severe errors are reported to the ChangelogManager (NavDrawer -> Logs);

3. To export any questionaire or form results to PDF, LabTablet uses [iTextPDF](http://itextpdf.com/);

4. Other "built-in" dependencies are Gson to handle avery json conversion and http response.

### Who do I talk to? ###

If you want to contact me (the developer), please feel free to do so through the email ricardo.amorim3@gmail.com.

* LabTablet was developed together with other relevant Information Systems projects at the Faculdade de Engenharia da Universidade do Porto. To know more about our environment and projects, follow [this link.](http://dendro.fe.up.pt/)


### Available at playstore ### 

<a href='https://play.google.com/store/apps/details?id=pt.up.fe.beta.labtablet&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>
