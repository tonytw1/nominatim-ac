$( "#location" ).autocomplete({
	source: function( request, response ) {	
		$.ajax({
			url: "https://nominatim-ac.eelpieconsulting.co.uk/search",
			cache: true,
			jsonpCallback : "callback",
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
		var osmUrl = osmUrlFor(ui.item.osmId, ui.item.osmType);
		$("#selected").html( ui.item ? '<a href="' + osmUrl + '" target="_blank">' + ui.item.osmId + "/" + ui.item.osmType + '</a>' : "Nothing selected");
		$("#latitude").text( ui.item ? ui.item.latitude : "");
		$("#longitude").text( ui.item ? ui.item.longitude : "");
	}

});

function osmUrlFor(osmId, osmType) {
	var osmTypes = {'N': 'node',
		'W': 'way',
		'R': 'relation'};

	return 'http://www.openstreetmap.org/' + osmTypes[osmType] + '/' + osmId;
}		
