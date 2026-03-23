
import java.util.Arrays;

public class Warstwa {
	Neuron [] neurony;
	int liczba_neuronow;
	double[] lastInputs;
	
	public Warstwa(){
		neurony=null;
		liczba_neuronow=0;
	}
	public Warstwa(int liczba_wejsc,int liczba_neuronow){
		this.liczba_neuronow=liczba_neuronow;
		neurony=new Neuron[liczba_neuronow];
		for(int i=0;i<liczba_neuronow;i++)
			neurony[i]=new Neuron(liczba_wejsc);
	}
	double [] oblicz_wyjscie(double [] wejscia){
		lastInputs=Arrays.copyOf(wejscia,wejscia.length);
		double [] wyjscie=new double[liczba_neuronow];
		for(int i=0;i<liczba_neuronow;i++)
			wyjscie[i]=neurony[i].oblicz_wyjscie(wejscia);
		return wyjscie;
	}
}
