import React, { Fragment } from 'react';
import axios from 'axios';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { useHistory } from 'react-router-dom';

import { Context } from './utils';

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
const PopupMarker = ({
  eventId,
  eventName,
  groupId,
  groupName,
  groupCountry,
  groupLon,
  groupLat,
  yesResponse,
  noResponse,
}) => {
  const dispatch = React.useContext(Context);
  return (
    <Marker
      icon={icon(groupCountry)}
      position={{ lat: groupLat, lng: groupLon }}
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
        dispatch({ type: 'countrySelected', payload: groupCountry });
      }}>
      <Popup>
        {eventName}({yesResponse})
      </Popup>
    </Marker>
  );
};

export default PopupMarker;
