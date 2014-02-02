/*
 * @author stuartb
 * @date 2008.10.08
 * @description Wizard forms made easy.
 */
jQuery.fn.wizard = function(settings)
{
    settings = jQuery.extend({
         show: function(element) { return true; },
         onPrevious: function(element) { return true; },
         onNext: function(element) { return true; },
         onChange: function(element) {return true},
         prevnext: true,
         submitpage: null
      }, settings);

    // Hide all pages save the first.
    jQuery(this).children(".wizardpage").hide();
    jQuery(this).children(".wizardpage:first").show();
    settings.show(jQuery(this).children(".wizardpage:first"));
    
    // Also highlight the first nav item.
    jQuery(this).children(".wizard-nav").children("a:first").addClass("active");
    
    // Wire progress thingy
    jQuery(this).children(".wizard-nav").children("a").click(function(){
        var target = jQuery(this).attr("href");
        jQuery(this).parent().parent().children(".wizardpage").hide();
        jQuery(target).fadeIn('slow');
        
        // mbaker@uams.edu: Add "onChange" function
        settings.onChange(target);
        
        settings.show(jQuery(target));
        jQuery(this).parent().children('a').removeClass('active');
        jQuery(this).addClass('active');
        return false;
    });
    
    // Prevent form submission on a wizard page...
    jQuery(this).children(".wizardpage").each(function(i){
        // unless there is a submit button on this page
        if((settings.submitpage == null && jQuery(this).find('input[type="submit"]').length < 1) ||
           (settings.submitpage != null && !jQuery(this).is(settings.submitpage)))
        {
            jQuery(this).find('input,select').keypress(function(event){
                return event.keyCode != 13;
            });
        }
    });
    
    if(settings.prevnext)
    {
        // Add prev/next step buttons
        jQuery(this).children(".wizardpage")
        .append('<div class="row wizardcontrols"></div>')
        .children(".wizardcontrols")
            .append('<input type="button" class="wizardprev" value="< Back" /><input type="button" class="wizardnext" value="Next >" />');
        jQuery('.wizardpage:first input[type="button"].wizardprev').hide(); // hide prev button on first page
        jQuery('.wizardpage:last input[type="button"].wizardnext').hide();  // hide next button on last page

        // Wire prev/next step buttons
        jQuery(this).children(".wizardpage")
        .children(".wizardcontrols")
        .children('input[type="button"].wizardprev').click(function(){
            var wizardpage = jQuery(this).parent().parent(); // wizardcontrols div, wizardpage div
            var wizardnav  = wizardpage.parent().children(".wizard-nav")
            
            // mbaker@uams.edu: Add "onPrevious" function
            settings.onPrevious(wizardpage.prev());
            
            wizardpage.hide();
            wizardpage.prev().show();
            settings.show(wizardpage.prev());
            
            try{ wizardpage.prev().find("input:first").focus(); } catch(err) {}
            wizardnav.children('a').removeClass('active');
            wizardnav.children('a[href="#' + wizardpage.attr('id') + '"]').prev().addClass('active');
        });
        jQuery(this).children(".wizardpage")
        .children(".wizardcontrols")
        .children('input[type="button"].wizardnext').click(function(){
            var wizardpage = jQuery(this).parent().parent(); // wizardcontrols div, wizardpage div
            var wizardnav  = wizardpage.parent().children(".wizard-nav")
            
            // mbaker@uams.edu: Add "onNext" function
            settings.onNext(wizardpage.prev());
            
            wizardpage.hide();
            wizardpage.next().show();
            settings.show(wizardpage.next());
            
            try{ wizardpage.prev().find("input:first").focus(); } catch(err) {}
            wizardpage.prev().find("input:first").focus();
            wizardnav.children('a').removeClass('active');
            wizardnav.children('a[href="#' + wizardpage.attr('id') + '"]').next().addClass('active');
        });
    }
    
    return jQuery(this);
};