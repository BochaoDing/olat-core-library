/**
 *  OpenOLAT theme JS extensions as jQuery module
 *  
 *  @dependencies jQuery
 */
+(function($) {

		var ThemeJS = function() {
			// nothing to do
		}

		/**
		 * Use the carrousel effect for background images on the login screen based 
		 * on the ooBgCarrousel OpenOLAT jQuery plugin
		 */
		ThemeJS.prototype.initDmzCarrousel = function() {
			this.dmzCarrousel = jQuery().ooBgCarrousel();
			this.dmzCarrousel.initCarrousel({
				query: "#o_body.o_dmz #o_bg", 
				images: ['login-bg1.jpg', 'login-bg2.jpg', 'login-bg3.jpg', 'login-bg4.jpg', 'login-bg5.jpg'],  
				shuffle: true,
				shuffleFirst: true,
				durationshow: 5000,
				durationout: 500,
				durationin: 500
			});
		}
		
		/**
		 * Adds a link to UZH to the footer. Image added via CSS
		 * 
		 * @method
		 */
		ThemeJS.prototype.addUZHLink = function() {
			// do not add footer twice
			if (jQuery("#o_footer_uzh").size() > 0) return;
			var footer = jQuery("#o_footer_container");
			if (!footer) return;
			var text = jQuery("<span id='o_footer_olatplus'>OLAT 10</span>");
			footer.prepend(text);
			var link = jQuery("<a id='o_footer_uzh' href='http://www.uzh.ch' target='_blank' title='Universität Zürich' />");
			footer.prepend(link);
		}

		
		/**
		 * Method to install the theme add-ons. Will be re-executed after each DOM replacement. 
		 * 
		 * @method
		 */
		ThemeJS.prototype.execThemeJS = function() {
			OPOL.themejs.addUZHLink();
		}
		
		//Execute when loading of page has been finished
		$(document).ready(function() {
			OPOL.themejs = new ThemeJS();
			OPOL.themejs.execThemeJS();			
			//OPOL.themejs.initDmzCarrousel();
			// execute after each dom replacement (navbar might have been changed)
			$(document).on("oo.dom.replacement.after", OPOL.themejs.execThemeJS);
		});
		
})(jQuery);
