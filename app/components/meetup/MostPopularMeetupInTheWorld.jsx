import React, { Fragment } from 'react';
import axios from 'axios';
import { Map, TileLayer } from 'react-leaflet';
import { getApiPath } from './utils';
import MarkersList from './MarkersList';
import HeatmapLayer from 'react-leaflet-heatmap-layer';
import { addressPoints } from '../data/realworld.10000';


const reducer = (state, action) => {
  if (action.type === 'replace') {
    return actio
  } else {
    return state;
  }
};

export const MostPopularMeetupInTheWorld = () => {
  const [state, dispatch] = React.useReducer(reducer, []);

  React.useEffect(() => {
    const fetchData = async () => {
      const response = await axios.get(getApiPath('events'));
      dispatch({ type: 'replace', payload: response.data });
    };
    fetchData().catch(error => {
      console.log(error);
    });
  }, []);

  return (
    <Map center={[51.505, -0.09]} zoom={2.457}>
      <MarkersList markers={state} />

      <TileLayer
          attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
    </Map>
  );
};
