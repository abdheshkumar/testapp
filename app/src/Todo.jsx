import React, { useReducer, useContext, useEffect, useRef } from "react";
const Context = React.createContext();
/**
 * Reducer for maintaining component's state
 * @param {*} state
 * @param {*} action
 */
function appReducer(state, action) {
  switch (action.type) {
    case "reset": {
      return action.payload;
    }
    case "add": {
      return [
        ...state,
        {
          id: Date.now(),
          text: "",
          completed: false,
        },
      ];
    }
    case "delete": {
      return state.filter(item => item.id !== action.payload);
    }
    case "completed": {
      return state.map(item => {
        if (item.id === action.payload) {
          return {
            ...item,
            completed: !item.completed,
          };
        }
        return item;
      });
    }
    default:
      return state;
  }
}

/**
 * Build todo app
 */
export default function TodosApp() {
  const [state, dispatch] = useReducer(appReducer, []);
  const handleClick = () => dispatch({ type: "add" });
  const didRun = useRef(false);
  useEffect(() => {
    const raw = localStorage.getItem("data");
    dispatch({ type: "reset", payload: JSON.parse(raw) });
  }, []);
  useEffect(() => localStorage.setItem("data", JSON.stringify(state)), [state]);
  return (
    <Context.Provider value={dispatch}>
      <h1>Todos App</h1>
      <button onClick={handleClick}>Add Todo</button>
      <TodosList items={state} />
    </Context.Provider>
  );
}

/**
 * List todos items
 * @param {*} param0
 */
const TodosList = ({ items }) => {
  return items.map(item => <TodoItem key={item.id} item={item} />);
};

/**
 * Show Todo item
 * @param {*} param0
 */
const TodoItem = ({ item }) => {
  const dispatch = useContext(Context);
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "row",
        justifyContent: "space-between",
      }}>
      <input
        type="checkbox"
        checked={item.completed}
        onChange={() => dispatch({ type: "completed", payload: item.id })}
      />
      <input type="text" defaultValue={item.text} />
      <button onClick={() => dispatch({ type: "delete", payload: item.id })}>
        Delete
      </button>

      {item.id}
    </div>
  );
};
