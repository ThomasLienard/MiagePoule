import React, {useEffect, useState} from "react";
import {getAgenda} from "../../services/agendaService.jsx";
import {formatDate, getRelativeTime} from "../../utils/dateFormatter.js";
import {Accordion, Card, ListGroup, ListGroupItem} from "react-bootstrap";

const Agenda = () => {
    const [tasks, setTasks] = useState([]);
    const [todayTasksMap, setTodayTasksMap] = useState(new Map());
    const [tomorrowTasksMap, setTomorrowTasksMap] = useState(new Map());

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchAgenda = async () => {
            try {
                const data = await getAgenda();
                setTasks(data)
            } catch (err) {
                setError("Erreur lors du chargement de l'agenda" + err);
            } finally {
                setLoading(false);
            }
        };

        fetchAgenda();
    }, []);

    useEffect(() => {
        const filterTasks = () => {
            const getTaskRelativeDay = (task) => {
                const date = task.event.timeSlot.start
                return getRelativeTime(date)
            }

            const todayTasks = tasks.filter(task => getTaskRelativeDay(task) === 'Today')
            const tomorrowTasks = tasks.filter(task => getTaskRelativeDay(task) === 'Tomorrow')

            const initMap = (map, tasks) => {
                for (const task of tasks) {
                    const mapKey = task.event.eventId
                    let currentTasks;
                    if (map.has(mapKey)) {
                        currentTasks = map.get(mapKey)
                    } else {
                        currentTasks = []
                    }
                    currentTasks.push(task)
                    map.set(mapKey, currentTasks)
                }
            }

            const todayMap = new Map()
            initMap(todayMap, todayTasks)
            setTodayTasksMap(todayMap)

            const tomorrowMap = new Map()
            initMap(tomorrowMap, tomorrowTasks)
            setTomorrowTasksMap(tomorrowMap)
        }

        filterTasks()
    }, [tasks]);

    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;
    return (
        <>
            <h2 className="text-center">📖 Mon agenda</h2>

            <div className="d-flex justify-content-center flex-md-row flex-column">
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <Card className="overflow-y-auto" style={{"height": "70vh"}}>
                        <Card.Header>
                            <Card.Title className="text-center">Aujourd'hui</Card.Title>
                        </Card.Header>
                        <Card.Body>
                            <Accordion>
                                {todayTasksMap.keys().toArray().map((key) => (
                                    <Accordion.Item eventKey={key}
                                                    key={`today-${key}`}
                                                    className="mb-1">
                                        <Accordion.Header>
                                            <div className="d-flex flex-column w-100 text-center">
                                                <span>{todayTasksMap.get(key)[0].event.eventName}</span>
                                                <span className="text-body-tertiary">{formatDate(todayTasksMap.get(key)[0].event.timeSlot.start)}</span>
                                            </div>
                                        </Accordion.Header>
                                        <Accordion.Body className="p-0">
                                            <ListGroup variant={"flush"}>
                                                {todayTasksMap.get(key).map(task => (
                                                    <ListGroupItem key={`today-task-${task.id}`}>
                                                        <div className="d-flex flex-column w-100">
                                                            <span>● {task.name}</span>
                                                            <span className="text-body-tertiary">{task.description}</span>
                                                        </div>
                                                    </ListGroupItem>
                                                ))}
                                            </ListGroup>
                                        </Accordion.Body>
                                    </Accordion.Item>
                                ))}
                            </Accordion>
                        </Card.Body>
                    </Card>
                </div>
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <Card className="overflow-y-auto" style={{"height": "70vh"}}>
                        <Card.Header>
                            <Card.Title className="text-center">Demain</Card.Title>
                        </Card.Header>
                        <Card.Body>
                            <Accordion>
                                {tomorrowTasksMap.keys().toArray().map((key) => (
                                    <Accordion.Item eventKey={key}
                                                    key={`tomorrow-${key}`}
                                                    className="mb-1">
                                        <Accordion.Header>
                                            <div className="d-flex flex-column w-100 text-center">
                                                <span>{tomorrowTasksMap.get(key)[0].event.eventName}</span>
                                                <span className="text-body-tertiary">{formatDate(tomorrowTasksMap.get(key)[0].event.timeSlot.start)}</span>
                                            </div>
                                        </Accordion.Header>
                                        <Accordion.Body className="p-0">
                                            <ListGroup variant={"flush"}>
                                                {tomorrowTasksMap.get(key).map(task => (
                                                    <ListGroupItem key={`tomorrow-task-${task.id}`}>
                                                        <div className="d-flex flex-column w-100">
                                                            <span>● {task.name}</span>
                                                            <span className="text-body-tertiary">{task.description}</span>
                                                        </div>
                                                    </ListGroupItem>
                                                ))}
                                            </ListGroup>
                                        </Accordion.Body>
                                    </Accordion.Item>
                                ))}
                            </Accordion>
                        </Card.Body>
                    </Card>
                </div>
            </div>

        </>
    );
}
export default Agenda;
