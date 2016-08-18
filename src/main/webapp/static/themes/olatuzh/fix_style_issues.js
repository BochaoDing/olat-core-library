/*********************************************************************************
 WORAROUND to fix style issues of (broken) custom content
 @author: Christian Meier <christian.meier3@uzh.ch>
 @see: identifiers_for_iframe_css_removal.js
 @see: content_correction.css
 @see: theme_correction.css

 REASONS:
 - Avoid the need for manual changes on each affected Course Element at GUI level if custom CSS breaks course layout
 - Configuration via GUI at Authoring CP and/or Course Element (Adapt layout = None) is not reliable for broken Content Packages
 - No CSS or JS will be injected into iframes by the backend if custom content can not be parsed correctly
 - Certain styles can not be overridden (e.g. deactivation of img-height=auto is impossible)
 - CourseCSS may break global theme layout if specific selectors for custom content are missing
 - Theme level changes require compiling workflow and deployment - bypass this overhead and allow quick style fixes at a central place

 ACTIONS:
 - Remove all courseCSS identified by "#o_css" and trailing counter from document => fixes major style problems due interferences with global theme styles
 - Remove content.css identified by "#themecss" from iframe for given identifiers_for_iframe_css_removal => fixes major style problems by resetting global theme styles
 - Inject an additional content_correction.css identified by "#content_correction_css" into iframe => fixes minor style problems in custom content
 - Inject an additional theme_correction.css identified by "#theme_correction_css" into document => fixes minor style interferences between global theme and custom content

 STEPS:
 1) Add Course using custom CSS identified by its RepositoryEntry and/or CourseNode to identifiers_for_iframe_css_removal.js
 2) Fix style issues at content_correction.css by css selectors identifying the specific Course (Element)
 3) Fix style issues at theme_correction.css by css selectors identifying the specific Course (Element)

 HISTORY:
 - 2016/07/29 cmeier: Initial version combining previous experiments
 - 2016/08/04 cmeier: Fixed event listening
 - 2016/08/10 cmeier: Improved implantCourseElementIDs
 - 2016/08/15 cmeier: Added more (2) Courses with Content Packages based on ELML using custom styles
 - 2016/08/17 cmeier: Added more (1) Courses with custom Layout using courseCSS

 ISSUES:
 - OLATNG-263: Initial entry from Authoring to Course does not set URL correctly
 => removal of content.css from iframe does not work, because RespositoryEntry/CourseNode is n/a in this case
 - OLATNG-264: Unexpected implantation of courseCSS into HTML-body while using Course Editor
 => custom css breaks layout in Authoring mode
 - OLATNG-265: Course Layout not applied to CP root node - dispose chain broken
 => configuration of course layout has no effect, if content package is entered at root node
/*********************************************************************************/

jQuery(document).ready(fix_style_issues);
jQuery(document).on("oo.dom.replacement.after", fix_style_issues);

function fix_style_issues() {

    var themePath = jQuery('script[src*=fix_style_issues]').attr('data-themePath');

    implantCourseElementIDs(jQuery("body"));

    jQuery("link[id^='o_css']").remove();

    if (jQuery(document).contents().find("#theme_correction_css").length == 0) {
        jQuery(document).contents().find("head").append(
            jQuery('<link/>', {
                id: "theme_correction_css",
                rel: "stylesheet",
                href: themePath + "theme_correction.css",
                type: "text/css"
            })
        );
    }

    jQuery("iframe").each(function () {

        var iframe = jQuery(this);

        iframe.load(function () {

            implantCourseElementIDs(iframe.contents().find("body"));

            var RepositoryEntry = iframe.contents().find("body").attr("data-repositoryentry");
            var CourseNode = iframe.contents().find("body").attr("data-coursenode");

            for (i in identifiers_for_iframe_css_removal) {
                var entry = identifiers_for_iframe_css_removal[i];
                if (entry.hasOwnProperty(RepositoryEntry)) {
                    if ((entry[RepositoryEntry] == CourseNode) || (entry[RepositoryEntry] == "all")) {
                        iframe.contents().find("#themecss").remove();
                    }
                }
            }

            if (iframe.contents().find("#content_correction_css").length == 0) {
                iframe.contents().find("head").append(
                    jQuery('<link/>', {
                        id: "content_correction_css",
                        rel: "stylesheet",
                        href: themePath + "content_correction.css",
                        type: "text/css"
                    })
                );
            }

            // Bypass prevention of cross-site-scripting to call b_sizeIframe() from iframe.js
            iframe.contents().find("body").trigger("click");
        });
    });
}

function implantCourseElementIDs(tag) {

    /*
     Parse identifiers of current Course Element from URL and implant into given tag.
     CAVEAT: This is not reliable for all circumstances! Identifiers may not available, if the URL is not set correctly by the backend.
     */

    var RepositoryEntry = location.pathname.match(/\/RepositoryEntry\/([0-9]*)/);
    var CourseNode = location.pathname.match(/\/CourseNode\/([0-9]*)/);

    if (RepositoryEntry instanceof Array) {
        if (RepositoryEntry.length === 2) {
            tag.attr("data-RepositoryEntry", RepositoryEntry[1]);
        }
    }
    if (CourseNode instanceof Array) {
        if (CourseNode.length === 2) {
            tag.attr("data-CourseNode", CourseNode[1]);
        }
    }
}