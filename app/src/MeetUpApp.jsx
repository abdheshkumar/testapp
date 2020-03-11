import React, {useEffect, useReducer} from "react"

const reducer = (state, action) => {
    if (action.type === "add") {
        return [action.payload, ...state]
    } else {
        return state
    }
}
const MeetUpApp = props => {
    const [state, dispatch] = useReducer(reducer, [])
    useEffect(() => {
        //e.g. make a fetch request, open a socket connection
        const ws = new WebSocket("ws://0.0.0.0:8080/rsvp")
        ws.onopen = () => {
            ws.send("Open connection..")
        };
        ws.onmessage = (event) => {
            dispatch({type: "add", payload: JSON.parse(event.data)})
        };
        ws.onclose = () => {
            console.log("In onclose....")
            ws.send("Close connection..")
        };
        return () => {
            //stuff that happens when the component unmounts
            //e.g. close socket connection
            ws.send("Close connection..")
            ws.close()
        }
    })
    return (state.map(rsvp => <RsvpItem key={rsvp.group.group_urlname} rsvp={rsvp}/>))
}

const RsvpItem = ({rsvp}) => {
    return (
        <div>{rsvp.group.group_name}</div>
    )
}
export default MeetUpApp