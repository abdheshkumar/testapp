// @flow

import React, { Component, Fragment } from 'react'
import { Map, TileLayer, Marker, Popup } from 'react-leaflet'
import data from '../data/data'

const MyPopupMarker = props => {
  return (
    <Marker
      position={props.position}
      onMouseOver={e => {
        e.target.openPopup()
      }}
      onMouseOut={e => {
        e.target.closePopup()
      }}
      onClick={e => {
        console.log('Click...')
      }}>
      <Popup>{props.eventName}</Popup>
    </Marker>
  )
}

const MyMarkersList = ({ markers }) => {
  const items = markers.map(({ eventId, ...props }) => (
    <MyPopupMarker key={eventId} {...props} />
  ))
  return <Fragment>{items}</Fragment>
}

export default class MostLocationInTheWorld extends Component {
  state = {
    markers: [
      {
        eventId: 'marker1',
        position: [51.5, -0.1],
        eventName: 'My first popup',
      },
      {
        eventId: 'marker2',
        position: [51.51, -0.1],
        eventName: 'My second popup',
      },
      {
        eventId: 'marker3',
        position: [51.49, -0.05],
        eventName: 'My third popup',
      },
    ] /* data.map(d => ({
      eventName: d.event.event_name,
      eventId: d.event.event_id,
      country: d.group.group_country,
      position: [d.group.group_lat, d.group.group_lon],
    })) */ /* [
      { key: 'marker1', position: [51.5, -0.1], content: 'My first popup' },
      { key: 'marker2', position: [51.51, -0.1], content: 'My second popup' },
      { key: 'marker3', position: [51.49, -0.05], content: 'My third popup' },
    ], */,
  }

  render() {
    const t = data.map(d => ({
      eventName: d.event.event_name,
      eventId: d.event.event_id,
      country: d.group.group_country,
      position: [d.group.group_lat, d.group.group_lon],
    }))
    console.log('*******Start')
    console.log(t)
    console.log('*******End')
    return (
      <Map
        center={[51.49, -0.05]}
        zoom={2.49}
        maxZoom={10}
        attributionControl={true}
        zoomControl={true}
        doubleClickZoom={true}
        scrollWheelZoom={true}
        dragging={true}
        animate={true}
        easeLinearity={0.35}>
        <TileLayer
          attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MyMarkersList markers={t} />
      </Map>
    )
  }
}
