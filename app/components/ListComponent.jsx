import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemText from '@material-ui/core/ListItemText';
import ListSubheader from '@material-ui/core/ListSubheader';
import Avatar from '@material-ui/core/Avatar';
import clsx from 'clsx';
import { extractLabel, useStyles } from './meetup/utils';
import Divider from '@material-ui/core/Divider';

const ListComponent = ({ data, fromMessageToListData, heading, ...props }) => {
  const classes = useStyles();
  return (
    <React.Fragment>
      {heading && (
        <Typography className={classes.text} variant="h5" gutterBottom>
          {heading}
        </Typography>
      )}

      <List className={classes.list}>
        {data.map(d => {
          const message = fromMessageToListData(d);
          return (
            <React.Fragment key={message.id}>
              <RenderListItem {...message} {...props}></RenderListItem>
            </React.Fragment>
          );
        })}
      </List>
    </React.Fragment>
  );
};

const RenderListItem = props => {
  const { id, primary, secondary, photo } = props;
  const name = extractLabel(primary);
  return (
    <>
      <ListItem button>
        <ListItemAvatar>
          {photo ? (
            <Avatar alt="Picture" src={photo} />
          ) : (
            <Avatar alt="Picture">{extractLabel(name)}</Avatar>
          )}
        </ListItemAvatar>
        <ListItemText primary={primary} secondary={secondary} />
      </ListItem>
      <Divider variant="inset" component="li" />
    </>
  );
};

export default ListComponent;
