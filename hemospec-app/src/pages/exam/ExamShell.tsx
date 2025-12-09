import { IonRouterOutlet } from '@ionic/react';
import { Route, Redirect } from 'react-router';
import PatientForm from '../PatientForm';
import AnalysisPage from '../AnalysisPage';

const ExamShell: React.FC = () => {
    return (
        <IonRouterOutlet>
            <Route exact path="/app/exam/patient" component={PatientForm} />
            <Route exact path="/app/exam/analysis" component={AnalysisPage} />
            <Route exact path="/app/exam">
                <Redirect to="/app/exam/patient" />
            </Route>
        </IonRouterOutlet>
    );
};

export default ExamShell;
