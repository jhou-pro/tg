define([
	'EntityStyling'
], function(
	EntityStyling) { 

	var StopEntityStyling = function() {
		EntityStyling.call(this);

		var self = this;
	};

	StopEntityStyling.prototype = Object.create(EntityStyling.prototype);
	StopEntityStyling.prototype.constructor = StopEntityStyling;

	StopEntityStyling.prototype.getColor = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Stop') {
				return "#FF4500"; //  orange;
			} else if (entity.properties._entityType === 'Message' || entity.properties._entityType === 'Summary_Message') {
				return EntityStyling.prototype.getColor.call(self, entity);
			} else {
				throw "StopEntityStyling.prototype.getColor: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Stop', 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "StopEntityStyling.prototype.getColor: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return StopEntityStyling;
});