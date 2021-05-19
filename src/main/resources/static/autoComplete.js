 var searchUrl = window.location.href + "search";
$( "#location" ).autocomplete({
	source: function( request, response ) {
		$.ajax({
			url: searchUrl,
			cache: true,
			method: "GET",
			data: {
				q: request.term,
				profile: $("#profile").val()					
			},
			success: function( data ) {
                $("#jsonPreview").text(JSON.stringify(data));
				response( $.map( data, function( item ) {
					return {
						label: (item.address + " (" + item.classification + "/" + item.type + ") "),
						value: item.address,
						id: item.id,
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
		$("#selected").html( ui.item ? '<a href="' + osmUrl + '" target="_blank">' + ui.item.id + '</a>' : "Nothing selected");
		$("#latitude").text( ui.item ? ui.item.latitude : "");
		$("#longitude").text( ui.item ? ui.item.longitude : "")
	}
});

function osmUrlFor(osmId, osmType) {
	var osmTypes = {'N': 'node',
		'W': 'way',
		'R': 'relation'};
	return 'http://www.openstreetmap.org/' + osmTypes[osmType] + '/' + osmId;
}
