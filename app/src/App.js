import React from "react";
import logo from "./logo.svg";
import "./App.css";
import Greetings from "./Greetings";
import GreetingsHooks from "./GreetingsHooks";
import TodosApp from "./Todo";
import * as a from "./ArrayApp";
import MeetUpApp from "./MeetUpApp"
function App() {
  return (
    <div className="App">
      <header>
        <MeetUpApp/>
       {/* <Greetings></Greetings>
        <GreetingsHooks />*/}
      </header>
    </div>
  );
}

export default App;
