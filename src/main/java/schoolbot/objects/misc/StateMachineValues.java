package schoolbot.objects.misc;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;

import java.util.List;
import java.util.stream.Collectors;

public class StateMachineValues
{
      private GuildMessageReceivedEvent messageReceivedEvent;
      private final long authorId;
      private final long channelId;
      private final JDA jda;
      private StateMachine machine;
      private CommandEvent event;
      private List<School> schoolList;
      private List<School> pittClass;
      private List<Classroom> classroomList;
      private List<Professor> professorList;
      private List<Assignment> assignmentList;
      private School school;
      private Classroom classroom;
      private Professor professor;
      private Assignment assignment;
      private int state = 1;

      public StateMachineValues(CommandEvent event, StateMachine machine)
      {
            this.event = event;
            this.schoolList = event.getGuildSchools();
            this.pittClass = event.getGuildSchools()
                    .stream()
                    .filter(School::isPittSchool)
                    .collect(Collectors.toList());
            this.classroomList = event.getGuildClasses();
            this.authorId = event.getUser().getIdLong();
            this.channelId = event.getChannel().getIdLong();
            this.jda = event.getJDA();
            this.school = null;
            this.classroom = null;
            this.assignment = null;
            this.professor = null;
            this.professorList = null;
            this.assignmentList = null;
            this.machine = machine;
      }

      public StateMachineValues(CommandEvent event)
      {
            this.event = event;
            this.schoolList = event.getGuildSchools();
            this.pittClass = event.getGuildSchools()
                    .stream()
                    .filter(School::isPittSchool)
                    .collect(Collectors.toList());
            this.classroomList = event.getGuildClasses();
            this.authorId = event.getUser().getIdLong();
            this.channelId = event.getChannel().getIdLong();
            this.jda = event.getJDA();
            this.school = null;
            this.classroom = null;
            this.assignment = null;
            this.professor = null;
            this.professorList = null;
            this.assignmentList = null;
            this.machine = null;
      }

      public GuildMessageReceivedEvent getMessageReceivedEvent()
      {
            return messageReceivedEvent;
      }

      public Professor getProfessor()
      {
            return professor;
      }

      public void setProfessor(Professor professor)
      {
            this.professor = professor;
      }

      public CommandEvent getEvent()
      {
            return event;
      }

      public void setEvent(CommandEvent event)
      {
            this.event = event;
      }

      public List<School> getSchoolList()
      {
            return schoolList;
      }

      public void setSchoolList(List<School> schoolList)
      {
            this.schoolList = schoolList;
      }

      public List<Assignment> getAssignmentList()
      {
            return assignmentList;
      }

      public void setAssignmentList(List<Assignment> assignmentList)
      {
            this.assignmentList = assignmentList;
      }

      public List<Professor> getProfessorList()
      {
            return professorList;
      }

      public void setProfessorList(List<Professor> professorList)
      {
            this.professorList = professorList;
      }

      public List<Classroom> getClassroomList()
      {
            return classroomList;
      }

      public void setClassroomList(List<Classroom> classroomList)
      {
            this.classroomList = classroomList;
      }

      public long getAuthorId()
      {
            return authorId;
      }

      public long getChannelId()
      {
            return channelId;
      }

      public void setMessageReceivedEvent(GuildMessageReceivedEvent messageReceivedEvent)
      {
            this.messageReceivedEvent = messageReceivedEvent;
      }

      public void setMachine(StateMachine machine)
      {
            this.machine = machine;
      }

      @Nullable
      public Assignment getAssignment()
      {
            return assignment;
      }

      public void setAssignment(Assignment assignment)
      {
            this.assignment = assignment;
      }

      @Nullable
      public Classroom getClassroom()
      {
            return classroom;
      }

      public void setClassroom(Classroom classroom)
      {
            if (school == null)
            {
                  this.school = classroom.getSchool();
            }
            this.classroom = classroom;
      }

      public JDA getJda()
      {
            return jda;
      }

      @Nullable
      public School getSchool()
      {
            return school;
      }

      public void setSchool(School school)
      {
            this.school = school;
      }


      public List<School> getPittClass()
      {
            return pittClass;
      }

      public void setPittClass(List<School> pittClass)
      {
            this.pittClass = pittClass;
      }

      @Nullable
      public StateMachine getMachine()
      {
            return machine;
      }

      public <T extends Paginatable> void setValue(T obj)
      {
            String className = obj.getClass().getSimpleName();

            switch (className)
            {
                  case "School" -> {
                        var school = (School) obj;
                        setSchool(school);
                  }

                  case "Professor" -> {
                        var professor = (Professor) obj;
                        setProfessor(professor);
                  }

                  case "Classroom" -> {
                        var classroom = (Classroom) obj;
                        setClassroom(classroom);

                  }

                  case "Assignment" -> {
                        var assignment = (Assignment) obj;
                        setAssignment(assignment);
                  }

                  default -> throw new IllegalStateException(String.format("%s is not supported", className));

            }

      }

      public void incrementMachineState()
      {
            state += 1;
      }

      public int getState()
      {
            return state;
      }

      public void setState(int state)
      {
            this.state = state;
      }

      @SuppressWarnings("unchecked")
      public <T extends Paginatable> void setList(List<T> list)
      {
            String className = list.get(0).getClass().getSimpleName();

            switch (className)
            {
                  case "School" -> {
                        var school = (List<School>) list;
                        setSchoolList(school);
                  }

                  case "Professor" -> {
                        var professor = (List<Professor>) list;
                        setProfessorList(professor);
                  }

                  case "Classroom" -> {
                        var classroom = (List<Classroom>) list;
                        setClassroomList(classroom);
                  }

                  case "Assignment" -> {
                        var assignment = (List<Assignment>) list;
                        setAssignmentList(assignment);
                  }

                  default -> throw new IllegalStateException(String.format("%s is not supported", className));


            }
      }


}


