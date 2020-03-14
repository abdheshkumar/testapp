import React from 'react';
import ListComponent from '../ListComponent';
import { RsvpMap } from './RsvpMap';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
/* import {useStyles} from './utils'; */

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing(2),
    textAlign: 'center',
    color: theme.palette.text.secondary,
    paddingBottom: 50,
  },
}));

/**
 * Display map and list of rsvp stream. This component drop old data after reaching to certain limit.
 * It skips invalid data like rsvp_id not available, duplicate records with the same rsvp_id, event_id, group_id etc.
 *
 */
const RsvpLive = () => {
  const classes = useStyles();
  const LIMIT = 500;
  const DROP_LIMIT = 100;
  const [state, setState] = React.useState({ rsvps: [], counter: 0 });
  const { rsvps } = state;

  React.useEffect(() => {
    const ws = new WebSocket('ws://localhost:8085/rsvp');
    ws.onopen = () => {
      console.log('Connected..');
    };
    ws.onmessage = event => {
      const message = JSON.parse(event.data);

      setState(prev => {
        const { rsvps, counter } = prev;
        const { rsvp_id, venue } = message;
        const skipDuplicateAndInvalidData =
          rsvp_id && venue && !rsvps.find(r => r.rsvp_id === rsvp_id);
        const data = skipDuplicateAndInvalidData
          ? { rsvps: [message, ...rsvps], counter: counter + 1 }
          : prev;
        return LIMIT < counter
          ? {
              rsvps: data.rsvps.splice(LIMIT - DROP_LIMIT, LIMIT),
              counter: counter - DROP_LIMIT,
            }
          : data;
      });
    };

    window.onbeforeunload = () => {
      console.log('Connection closing on page unload');
      ws.close();
    };
    ws.onclose = () => {
      console.log('Disconnected..');
    };
    return () => {
      const wsState = ws.readyState;
      return wsState !== WebSocket.CLOSED || wsState !== WebSocket.CONNECTING
        ? ws.close()
        : null;
    };
  }, []);

  return (
    <Grid container justify="center" spacing={2}>
      <Grid sm={9} item>
        <Paper className={classes.paper}>
          <RsvpMap state={rsvps}></RsvpMap>
        </Paper>
      </Grid>
      <Grid sm={3} item>
        <Paper
          className={classes.paper}
          style={{ overflowY: 'scroll', height: '600px' }}>
          <RsvpLiveList rsvps={rsvps} />
        </Paper>
      </Grid>
    </Grid>
  );
};

/**
 * Display live list of rsvp
 * @param {*} param0
 */
const RsvpLiveList = ({ rsvps }) => {
  return (
    <Typography component="div" style={{ height: '85vh' }}>
      <ListComponent
        data={rsvps}
        fromMessageToListData={message => ({
          id: message.rsvp_id,
          primary: message.member.member_name,
          photo: message.member.photo,
          secondary: message.event.event_name,
        })}
        heading="Live Rsvp"
      />
    </Typography>
  );
};

export default RsvpLive;
