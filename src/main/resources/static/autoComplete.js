$( "#location" ).autocomplete({
	source: function( request, response ) {	
		$.ajax({
			url: "http://nominatim-ac.eelpieconsulting.co.uk/search",
			cache: true,
			dataType: "jsonp",
			data: {
				q: request.term,
				profile: $("#profile").val()					
			},
			success: function( data ) {
				response( $.map( data, function( item ) {				
					return {
						label: (item.address + " (" + item.classification + "/" + item.type + ")"),
						value: item.address,
						osmId: item.osmId,
						osmType: item.osmType,
						latitude: item.latlong.lat,
						longitude: item.latlong.lon					
					}
				}));
			}
		});
	},

	select: function( event, ui ) {
		$("#selected").text( ui.item ? ui.item.osmId + "/" + ui.item.osmType : "Nothing selected");
		$("#latitude").text( ui.item ? ui.item.latitude : "");
		$("#longitude").text( ui.item ? ui.item.longitude : "");
	}

});
		