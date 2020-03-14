import React, { Fragment } from 'react';
import axios from 'axios';
import { Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { useHistory } from 'react-router-dom';

const icon = L.divIcon({
  className: 'custom-div-icon',
  html:
    "<div style='background-color:#4838cc;' class='marker-pin'></div><i class='material-icons'>accessible</i>",
  iconSize: [30, 42],
  iconAnchor: [15, 42],
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
  const history = useHistory();
  const redirectOnTrendingPage = country => {
    history.push(`/trending-topics-by-country/${country}`);
  };
  return (
    <Marker
      icon={icon}
      position={{ lat: groupLat, lng: groupLon }}
      onMouseOver={e => {
        e.target.openPopup();
      }}
      onMouseOut={e => {
        e.target.closePopup();
      }}
      onClick={e => {
        redirectOnTrendingPage(groupCountry);
        console.log('Click...');
      }}>
      <Popup>
        {eventName}({yesResponse})
      </Popup>
    </Marker>
  );
};

export default PopupMarker;
