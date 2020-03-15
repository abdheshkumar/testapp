import React from 'react';
import { Map, Marker, Popup, TileLayer } from 'react-leaflet';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemText from '@material-ui/core/ListItemText';
import Avatar from '@material-ui/core/Avatar';
import Typography from '@material-ui/core/Typography';
import { useStyles } from './utils';

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

const icon = colorCode =>
  L.divIcon({
    className: '',
    html: `<div><i class='material-icons' style='font-size:50px;color:${colorCode}'>location_on</i></div>`,
    iconSize: [20, 20], // size of the icon
    shadowSize: [50, 64], // size of the shadow
    iconAnchor: [15, 82], // point of the icon which will correspond to marker's location
    shadowAnchor: [4, 62], // the same for the shadow
    popupAnchor: [5, -68], // point from which the popup should open relative to the iconAnchor
  });

const RsvpPopupMarker = props => {
  const { venue_name, lon, lat, venue_id } = props.venue;
  const colorCode = props.response === 'yes' ? '#005005' : '#b4004e';
  return (
    <Marker
      icon={icon(colorCode)}
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
      }}>
      <Popup>
        <PopupLayout {...props} />
      </Popup>
    </Marker>
  );
};

const PopupLayout = props => {
  const classes = useStyles();
  console.log(props);
  const { primary, venue_name, member, event } = props;
  const { photo, member_name } = member;
  const { event_name } = event;
  return (
    <>
      <ListItem>
        <ListItemAvatar>
          <Avatar alt="Picture" src={photo} />
        </ListItemAvatar>
        <ListItemText primary={member_name} />
      </ListItem>
      <ListItem>
        <ListItemText primary={event_name} />
      </ListItem>
    </>
  );
};
