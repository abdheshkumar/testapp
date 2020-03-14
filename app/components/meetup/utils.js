import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
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
  text: {
    padding: theme.spacing(2, 2, 0),
  },

  list: {
    marginBottom: theme.spacing(2),
  },
  subheader: {
    backgroundColor: theme.palette.background.paper,
  },

  grow: {
    flexGrow: 1,
  },
}));

const getApiPath = path => `http://localhost:8085/${path}`;

const Context = React.createContext();

export function extractLabel(name) {
  const words = name
    .trim()
    .split(' ')
    .filter(word => !!word);
  const firstCharOfFirstWord = words[0] && words[0][0] ? words[0][0] : '';
  const firstCharOfSercondWord = words[1] && words[1][0] ? words[1][0] : '';
  const firstCharOfThirdWord = words[2] && words[2][0] ? words[2][0] : '';
  return firstCharOfFirstWord + firstCharOfSercondWord + firstCharOfThirdWord;
}

export { getApiPath, Context, useStyles };
