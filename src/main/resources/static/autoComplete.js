$( "#location" ).autocomplete({
	source: function( request, response ) {
		$.ajax({
			url: "/suggest",
			dataType: "jsonp",
			data: {
				q: request.term							
			},
			success: function( data ) {
				response( $.map( data, function( item ) {
				
					console.log(item);
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
		