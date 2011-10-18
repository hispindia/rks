

var EVT =
{
	ready : function()
	{
		/**
		 * Page Actions
		 */
		var enableCheck = true;
		var pageId = jQuery("#pageId").val();
		if(enableCheck && pageId != undefined && pageId != '' && pageId != null && eval("CHECK." + pageId))
		{
			eval("CHECK." + pageId + "()");
		}

		/**
		 * Ajax Indicator when send and receive data
		 */
		if(jQuery.browser.msie)
		{
			jQuery.ajaxSetup({cache: false});
		}
	
	}
};

var CHECK = 
{
	
	
	categoryPage : function()
	{
		var validator = jQuery("#categoryForm").validate(
		{
			event : "blur",
			rules : 
			{
			
				"name" : { required : true}
				
			}
		});
	},
	itemPage : function()
	{
		jQuery('.date-pick').datepicker({yearRange:'c-30:c+30', dateFormat: 'dd/mm/yy', changeMonth: true, changeYear: true});
		var validator = jQuery("#itemForm").validate(
		{
			event : "blur",
			rules : 
			{
			
				"category" : { required : true},
				"transactionType" : { required : true},
				"dateIncomeOutcome" : { required : true},
				"amount" : { required : true,number: true}
				
			}
		});
	},
	itemListPage : function()
	{
		jQuery('.date-pick').datepicker({yearRange:'c-30:c+30', dateFormat: 'dd/mm/yy', changeMonth: true, changeYear: true});
		
	}
	
};

/**
 * Pageload actions trigger
 */

jQuery(document).ready(
	function() 
	{
		EVT.ready();
	}
);



