import React from 'react';
import { Map, Marker, Popup, TileLayer } from 'react-leaflet';

export const RsvpMap = ({ state }) => {
  return (
    <Map center={[51.505, -0.09]} zoom={0.7}>
      <RsvpMarkersList markers={state} />
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
    </Map>
  );
};

const RsvpMarkersList = ({ markers }) => {
  return (
    <>
      {markers.map(({ rsvp_id, ...props }) => (
        <RsvpPopupMarker key={rsvp_id} {...props} />
      ))}
    </>
  );
};

const icon = country =>
  L.divIcon({
    /* className: 'custom-div-icon', */
    html: `<span class='flag-icon flag-icon-${country}'></span>`,
    iconSize: [20, 20], // size of the icon
    shadowSize: [50, 64], // size of the shadow
    iconAnchor: [15, 82], // point of the icon which will correspond to marker's location
    shadowAnchor: [4, 62], // the same for the shadow
    popupAnchor: [-3, -76], // point from which the popup should open relative to the iconAnchor
  });

const RsvpPopupMarker = props => {
  const { venue_name, lon, lat, venue_id } = props.venue;
  return (
    <Marker
      //icon={icon(groupCountry)}
      position={{ lat: lat, lng: lon }}
      onMouseOver={e => {
        e.target.openPopup();
      }}
      onMouseOut={e => {
        e.target.closePopup();
      }}
      onClick={e => {
        console.log(
          'dispatching an action for showing trending topics of the country...',
        );
        //dispatch({ type: 'countrySelected', payload: groupCountry });
      }}>
      <Popup>{venue_name}</Popup>
    </Marker>
  );
};
