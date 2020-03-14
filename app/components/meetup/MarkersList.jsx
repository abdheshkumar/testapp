import React from 'react';
import PopupMarker from './PopupMarker';

const MarkersList = ({ markers }) => {
  return (
    <>
      {markers.map(({ eventId, ...props }) => (
        <PopupMarker key={eventId} {...props} />
      ))}
    </>
  );
};

export default MarkersList;
