Q(document)
		.ready(
				function() {

					var concurrentBuild = Q("input:checkbox[name='_.concurrentBuild']");
					
					checkConcurrentExecutionValues();

					concurrentBuild.bind('change', function() {
						checkConcurrentExecutionValues();
					});
					
					
					Q('input:radio[name$=triggerIfResult]').change( function() {
						checkConcurrentExecutionValues();
					});
					
					
					function checkConcurrentExecutionValues() {
						if ( concurrentBuild.is(":checked") && (Q('input:radio[name$=triggerIfResult]:checked').val() != "allowMultiSelectionForConcurrentBuilds" ) ) {
							Q("#allowmultinodeselection").show();
						} else if ( !concurrentBuild.is(":checked") && (Q('input:radio[name$=triggerIfResult]:checked').val() == "allowMultiSelectionForConcurrentBuilds" ) ) {
							Q("#allowmultinodeselection").show();
						} else {
							Q("#allowmultinodeselection").hide();
						}
					}

				});