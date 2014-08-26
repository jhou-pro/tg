require.config({
	// waitSeconds : 120,
	paths: {
		// 'jQuery': 'vendor/jquery-1.9.0.min',
		// 'underscore': 'vendor/underscore-1.9.min',
		'async': 'vendor/async', // AMD-compliant
		'leaflet': 'vendor/leaflet/leaflet', // AMD-compliant

		'googlemaps': 'vendor/leaflet/providers/googlemaps', // an 'async' requirejs plugin wrapper for google maps loading -- AMD-compliant
		'leaflet.googlemaps': 'vendor/leaflet/providers/Google', // non-AMD module
		'yandexmaps': "http://api-maps.yandex.ru/2.0/?load=package.map&lang=ru-RU", // non-AMD module // TODO: lang RU??		
		'leaflet.yandexmaps': 'vendor/leaflet/providers/Yandex', // non-AMD module

		'leaflet.draw': 'vendor/leaflet/draw/leaflet.draw', // non-AMD module
		'leaflet.markercluster': 'vendor/leaflet/markercluster/leaflet.markercluster', // non-AMD module
		'leaflet.loadingcontrol': 'vendor/leaflet/controlloading/Control.Loading',
		'leaflet.easybutton': 'vendor/leaflet/easybutton/easy-button', // non-AMD module
		'leaflet.markerrotation': 'vendor/leaflet/markerrotation/Marker.Rotate' // non-AMD module
	},
	shim: { // used for non-AMD modules

		// 'jQuery': {
		//     exports: '$'
		// },
		// 'underscore': {
		//     exports: '_'
		// },

		// MAP PROVIDERS:
		'leaflet.googlemaps': {
			deps: ['leaflet', 'googlemaps!'],
			exports: 'LeafletGoogleMaps'
		},
		'yandexmaps': {
			deps: [],
			exports: 'YandexMaps'
		},
		'leaflet.yandexmaps': {
			deps: ['leaflet', 'yandexmaps'],
			exports: 'LeafletYandexMaps'
		},

		// MAP DRAWING:
		'leaflet.draw': {
			deps: ['leaflet'],
			exports: 'LeafletDraw'
		},

		// MAP CLUSTERING:
		'leaflet.markercluster': {
			deps: ['leaflet'],
			exports: 'LeafletMarkercluster'
		},

		'leaflet.easybutton': {
			deps: ['leaflet'],
			exports: 'LeafletEasybutton'
		},

		'leaflet.markerrotation': {
			deps: ['leaflet'],
			exports: 'LeafletMarkerrotation'
		}
	},
	googlemaps: { // configuration for 'async' requirejs plugin wrapper (google maps loading)
		url: 'http://maps.google.com/maps/api/js',
		// url: 'https://maps.googleapis.com/maps/api/js',
		params: {
			'v': '3.2',
			// key: 'abcd1234',
			// libraries: 'geometry',
			sensor: false // Defaults to false
		}
		// async: asyncLoaderPlugin // Primarly for providing test stubs.
	}
});

require(['log', 'GisComponent'], function(log, GisComponent) {
	var _gisComponent = new GisComponent();

	log(_gisComponent);

	window.gisComponent = _gisComponent;
});